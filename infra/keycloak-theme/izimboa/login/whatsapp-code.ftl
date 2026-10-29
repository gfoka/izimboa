<#-- Connexion par code WhatsApp, étape 2 : le code reçu (extension izimboa-whatsapp-otp). -->
<#import "template.ftl" as layout>
<#import "field.ftl" as field>
<#import "buttons.ftl" as buttons>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('code'); section>
<!-- template: whatsapp-code.ftl -->
    <#if section = "header">
        ${msg("waCodeTitle")}
    <#elseif section = "form">
        <p class="izimboa-wa-lead">${msg("waCodeLead", waPhone!'')}</p>
        <form id="kc-whatsapp-code-form" class="${properties.kcFormClass!}" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
            <#assign codeError = kcSanitize(messagesPerField.get('code'))?no_esc>
            <@field.group name="code" label=msg("waCodeLabel") error=codeError required=true>
                <span class="${properties.kcInputClass} <#if codeError?has_content>${properties.kcError}</#if>">
                    <input id="code" name="code" type="text" inputmode="numeric" pattern="[0-9 ]*" autocomplete="one-time-code"
                           maxlength="7" autofocus required aria-invalid="<#if codeError?has_content>true</#if>"/>
                    <@field.errorIcon error=codeError/>
                </span>
            </@field.group>
            <@buttons.actionGroup>
                <@buttons.button id="kc-login" name="login" label="doLogIn" class=["kcButtonPrimaryClass", "kcButtonBlockClass"] />
            </@buttons.actionGroup>
        </form>
        <form id="kc-whatsapp-resend-form" class="izimboa-wa-secondary" action="${url.loginAction}" method="post" novalidate>
            <button id="wa-resend" type="submit" name="resend" value="1" class="${properties.kcButtonSecondaryClass!} ${properties.kcButtonBlockClass!}"
                    data-wait="${(waResendAfter!0)?c}" data-label="${msg('waResend')}" data-wait-label="${msg('waResendIn')}">${msg("waResend")}</button>
            <button type="submit" name="changePhone" value="1" class="izimboa-wa-link">${msg("waChangePhone")}</button>
        </form>
        <script>
            (function () {
                var b = document.getElementById("wa-resend");
                var left = parseInt(b.getAttribute("data-wait"), 10) || 0;
                function tick() {
                    if (left > 0) {
                        b.disabled = true;
                        b.textContent = b.getAttribute("data-wait-label").replace("{0}", left);
                        left--;
                        setTimeout(tick, 1000);
                    } else {
                        b.disabled = false;
                        b.textContent = b.getAttribute("data-label");
                    }
                }
                tick();
            })();
        </script>
    </#if>
</@layout.registrationLayout>
