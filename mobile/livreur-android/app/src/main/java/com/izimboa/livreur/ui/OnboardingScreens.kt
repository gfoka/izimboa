package com.izimboa.livreur.ui

import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.izimboa.livreur.data.DocRequirement
import com.izimboa.livreur.data.MyFile
import com.izimboa.livreur.onboarding.Photos
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Actions du dossier, fournies par l'écran principal. Chacune renvoie true si elle a réussi. */
class FileActions(
    val saveProfile: suspend (String, String, String?) -> Boolean,
    val upload: suspend (String, String?, String?, Map<Int, ByteArray>) -> Boolean,
    val submit: suspend () -> Boolean,
    val refresh: suspend () -> Unit,
    val logout: () -> Unit,
    val dismiss: () -> Unit,
)

private val DATE_FR: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/**
 * Dossier du livreur : identité, puis chaque document page par page, puis envoi. Un écran à la fois, un grand
 * bouton par écran. {@code onClose} : retour à l'accueil (livreur déjà actif qui complète son dossier), sinon null.
 */
@Composable
fun CourierFileScreen(file: MyFile, busy: Boolean, message: String?, actions: FileActions, onClose: (() -> Unit)?) {
    var screen by rememberSaveable { mutableStateOf(if (file.firstName.isNullOrBlank()) "profile" else "hub") }
    val scope = rememberCoroutineScope()
    when {
        file.status == "SUSPENDED" -> StatusScreen(
            title = "Compte suspendu",
            text = (file.reason?.let { "Motif : $it\n\n" } ?: "") + "Contactez Izimboa pour en savoir plus.",
            actions = actions, busy = busy,
        )
        file.status == "PENDING" && screen == "hub" -> StatusScreen(
            title = "Dossier envoyé ✓",
            text = "Izimboa vérifie vos documents. Dès que votre compte est validé, vous pourrez passer en ligne et recevoir des courses.",
            actions = actions, busy = busy, onEdit = { screen = "docs" },
        )
        screen == "profile" -> ProfileStep(file, busy, message, actions.dismiss,
            onBack = if (file.firstName.isNullOrBlank() && onClose == null) null else ({ screen = "hub" }),
            onSave = { f, l, e -> scope.launch { if (actions.saveProfile(f, l, e)) screen = "hub" } })
        screen.startsWith("doc:") -> {
            val req = file.documents.firstOrNull { it.type.code == screen.removePrefix("doc:") }
            if (req == null) screen = "hub"
            else DocumentStep(req, busy, message, actions.dismiss, onBack = { screen = "hub" },
                onSend = { number, expires, pages -> scope.launch { if (actions.upload(req.type.code, number, expires, pages)) screen = "hub" } })
        }
        else -> Hub(file, busy, message, actions, onClose,
            onProfile = { screen = "profile" }, onDocument = { screen = "doc:$it" },
            onSubmit = { scope.launch { if (actions.submit() && file.status == "ACTIVE") onClose?.invoke() } })
    }
}

@Composable
private fun Page(title: String, onBack: (() -> Unit)?, content: @Composable () -> Unit, bottom: @Composable () -> Unit) {
    onBack?.let { BackHandler(onBack = it) }
    Column(Modifier.fillMaxSize().background(Brand.Cloud).statusBarsPadding().navigationBarsPadding().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 24.dp, top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = Brand.Navy)
                }
            } else Spacer(Modifier.size(16.dp))
            Text(title, style = MaterialTheme.typography.headlineMedium, color = Brand.Navy)
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) { content() }
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { bottom() }
    }
}

@Composable
private fun StatusScreen(title: String, text: String, actions: FileActions, busy: Boolean, onEdit: (() -> Unit)? = null) {
    val scope = rememberCoroutineScope()
    Page(title, onBack = null, content = {
        Spacer(Modifier.height(24.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = Brand.Ink)
    }, bottom = {
        BigButton("ACTUALISER", { scope.launch { actions.refresh() } }, loading = busy)
        if (onEdit != null) SecondaryButton("Voir ou corriger mes documents", onEdit)
        SecondaryButton("Se déconnecter", actions.logout, color = Brand.Slate)
    })
}

@Composable
private fun Hub(
    file: MyFile, busy: Boolean, message: String?, actions: FileActions, onClose: (() -> Unit)?,
    onProfile: () -> Unit, onDocument: (String) -> Unit, onSubmit: () -> Unit,
) {
    Page("Votre dossier livreur", onBack = onClose, content = {
        Text("Pour recevoir des courses, Izimboa vérifie votre identité. Touchez chaque ligne pour la compléter.",
            style = MaterialTheme.typography.bodyLarge, color = Brand.Slate)
        if (file.status == "REJECTED") Banner("Dossier à corriger" + (file.reason?.let { " : $it" } ?: "."))
        file.deadline?.let { Banner("À compléter avant le ${LocalDate.parse(it).format(DATE_FR)} pour continuer à recevoir des courses.") }
        if (message != null) Banner(message, onClose = actions.dismiss)
        Row_(
            title = "Votre identité",
            detail = listOfNotNull(file.firstName, file.lastName).joinToString(" ").ifBlank { "Prénom et nom" },
            done = !file.firstName.isNullOrBlank() && !file.lastName.isNullOrBlank(), onClick = onProfile,
        )
        file.documents.forEach { req ->
            val (label, done) = when (req.state) {
                "OK" -> "Fourni" to true
                "EXPIRING_SOON" -> "Expire bientôt : renouvelez-le" to true
                "EXPIRED" -> "Expiré : à renouveler" to false
                else -> (if (req.type.mandatory) "À fournir" else "Facultatif") to false
            }
            Row_(title = req.type.name, detail = label, done = done, onClick = { onDocument(req.type.code) })
        }
    }, bottom = {
        BigButton(if (file.status == "ACTIVE") "TERMINER" else "ENVOYER MON DOSSIER", onSubmit, enabled = file.complete, loading = busy)
        if (!file.complete) Text("Complétez les lignes marquées « À fournir » pour envoyer votre dossier.",
            style = MaterialTheme.typography.bodyMedium, color = Brand.Slate)
        if (onClose == null) SecondaryButton("Se déconnecter", actions.logout, color = Brand.Slate)
    })
}

@Composable
private fun Row_(title: String, detail: String, done: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White)
            .clickableNoRipple(onClick).padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = Brand.Navy)
            Text(detail, style = MaterialTheme.typography.bodyLarge, color = if (done) Brand.Go else Brand.Sun)
        }
        Icon(if (done) Icons.Filled.CheckCircle else Icons.Filled.CameraAlt, contentDescription = null,
            tint = if (done) Brand.Go else Brand.Sun, modifier = Modifier.size(34.dp))
    }
}

@Composable
private fun ProfileStep(file: MyFile, busy: Boolean, message: String?, dismiss: () -> Unit, onBack: (() -> Unit)?,
                        onSave: (String, String, String?) -> Unit) {
    var first by rememberSaveable { mutableStateOf(file.firstName ?: "") }
    var last by rememberSaveable { mutableStateOf(file.lastName ?: "") }
    var email by rememberSaveable { mutableStateOf(file.email ?: "") }
    Page("Votre identité", onBack, content = {
        Text("Comme sur votre carte d'identité.", style = MaterialTheme.typography.bodyLarge, color = Brand.Slate)
        Field("Prénom", first, { first = it }, KeyboardType.Text, KeyboardCapitalization.Words)
        Field("Nom", last, { last = it }, KeyboardType.Text, KeyboardCapitalization.Words)
        Field("E-mail (facultatif)", email, { email = it }, KeyboardType.Email, KeyboardCapitalization.None)
        if (message != null) Banner(message, onClose = dismiss)
    }, bottom = {
        BigButton("CONTINUER", { onSave(first, last, email) }, enabled = first.isNotBlank() && last.isNotBlank(), loading = busy)
    })
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit, type: KeyboardType, caps: KeyboardCapitalization) {
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) }, singleLine = true,
        textStyle = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(capitalization = caps, keyboardType = type, imeAction = ImeAction.Next),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentStep(req: DocRequirement, busy: Boolean, message: String?, dismiss: () -> Unit, onBack: () -> Unit,
                         onSend: (String?, String?, Map<Int, ByteArray>) -> Unit) {
    val t = req.type
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var number by rememberSaveable { mutableStateOf(req.submitted?.number ?: "") }
    var expires by rememberSaveable { mutableStateOf(req.submitted?.expiresOn) }
    var picking by remember { mutableStateOf(false) }
    val photos = remember { mutableStateMapOf<Int, ByteArray>() }
    var pending by remember { mutableStateOf<Pair<Int, File>?>(null) }
    var photoError by remember { mutableStateOf<String?>(null) }

    val camera = rememberLauncherForActivityResult(Photos.TakePhoto()) { ok ->
        val p = pending ?: return@rememberLauncherForActivityResult
        pending = null
        if (!ok) { p.second.delete(); return@rememberLauncherForActivityResult }
        scope.launch {
            val bytes = withContext(Dispatchers.IO) { Photos.shrink(p.second) }
            if (bytes == null) photoError = "Photo illisible : reprenez-la." else photos[p.first] = bytes
        }
    }

    Page(t.name, onBack, content = {
        Text(t.help ?: "Photographiez le document en entier, bien lisible, sans reflet.",
            style = MaterialTheme.typography.bodyLarge, color = Brand.Slate)
        if (req.submitted != null) Text("Déjà envoyé. Reprenez les photos pour le remplacer.",
            style = MaterialTheme.typography.bodyMedium, color = Brand.Go)
        t.pages.forEach { page ->
            val shot = photos[page.id]
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White)
                    .border(2.dp, if (shot != null) Brand.Go else Brand.Line, RoundedCornerShape(20.dp))
                    .clickableNoRipple {
                        try {
                            val (f, uri) = Photos.newCapture(context)
                            pending = page.id to f
                            camera.launch(Photos.TakePhoto.Request(uri, page.camera == "FRONT"))
                        } catch (e: Exception) {
                            photoError = "Impossible d'ouvrir l'appareil photo."
                        }
                    }.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (shot != null) Icons.Filled.CheckCircle else Icons.Filled.CameraAlt, contentDescription = null,
                        tint = if (shot != null) Brand.Go else Brand.Sun, modifier = Modifier.size(34.dp))
                    Spacer(Modifier.size(12.dp))
                    Text(if (shot != null) "${page.name} : reprendre" else "Photographier : ${page.name}",
                        style = MaterialTheme.typography.titleLarge, color = Brand.Navy)
                }
                if (shot != null) {
                    val bmp = remember(shot) { BitmapFactory.decodeByteArray(shot, 0, shot.size)?.asImageBitmap() }
                    if (bmp != null) Image(bmp, contentDescription = page.name, contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)))
                }
            }
        }
        if (t.numberRequirement != "NONE") {
            Field(if (t.numberRequirement == "REQUIRED") "Numéro du document" else "Numéro (facultatif)",
                number, { number = it }, KeyboardType.Ascii, KeyboardCapitalization.Characters)
        }
        if (t.expiryRequired) {
            SecondaryButton(expires?.let { "Expire le ${LocalDate.parse(it).format(DATE_FR)}" } ?: "Date d'expiration : choisir",
                { picking = true })
        }
        photoError?.let { Banner(it, onClose = { photoError = null }) }
        if (message != null) Banner(message, onClose = dismiss)
    }, bottom = {
        val ready = t.pages.all { photos.containsKey(it.id) } &&
            (t.numberRequirement != "REQUIRED" || number.isNotBlank()) && (!t.expiryRequired || expires != null)
        BigButton("ENVOYER CE DOCUMENT", { onSend(number.ifBlank { null }, expires, photos.toMap()) }, enabled = ready, loading = busy)
    })

    if (picking) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = expires?.let { LocalDate.parse(it).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() },
        )
        DatePickerDialog(
            onDismissRequest = { picking = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { expires = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString() }
                    picking = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { picking = false }) { Text("Annuler") } },
        ) { DatePicker(state = state) }
    }
}

/** Bandeau de l'accueil : dossier à compléter (livreur déjà actif) ou document qui expire bientôt. */
@Composable
fun FileReminder(file: MyFile, onOpen: () -> Unit) {
    val expiring = file.documents.any { it.state == "EXPIRING_SOON" && it.type.mandatory }
    val text = when {
        !file.complete -> "Complétez votre dossier" + (file.deadline?.let { " avant le ${LocalDate.parse(it).format(DATE_FR)}" } ?: "") + "."
        expiring -> "Un document expire bientôt : renouvelez-le."
        else -> return
    }
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Brand.SunSoft).clickableNoRipple(onOpen).padding(16.dp)) {
        Text("$text  Toucher ici ›", style = MaterialTheme.typography.bodyLarge, color = Brand.Navy)
    }
}
