<#-- Connexion par code WhatsApp, étape 1 : le numéro (extension izimboa-whatsapp-otp). -->
<#import "template.ftl" as layout>
<#import "field.ftl" as field>
<#import "buttons.ftl" as buttons>
<#import "social-providers.ftl" as identityProviders>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('phone'); section>
<!-- template: whatsapp-phone.ftl -->
    <#if section = "header">
        ${msg("waPhoneTitle")}
    <#elseif section = "form">
        <p class="izimboa-wa-lead">${msg("waPhoneLead")}</p>
        <#if waUnavailable??>
            <p class="izimboa-wa-note">${msg("waUnavailable")}</p>
        </#if>
        <form id="kc-whatsapp-phone-form" class="${properties.kcFormClass!}" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
            <#assign phoneError = kcSanitize(messagesPerField.get('phone'))?no_esc>
            <@field.group name="phone" label=msg("waPhoneLabel") error=phoneError required=true>
                <span class="${properties.kcInputClass} <#if phoneError?has_content>${properties.kcError}</#if>">
                    <input id="phone" name="phone" value="${waPhoneValue!''}" type="tel" inputmode="tel" autocomplete="tel"
                           placeholder="6 99 12 34 56" maxlength="30" autofocus required
                           aria-invalid="<#if phoneError?has_content>true</#if>" aria-describedby="phone-hint"/>
                    <@field.errorIcon error=phoneError/>
                </span>
                <p id="phone-hint" class="izimboa-wa-hint">${msg("waPhoneHint")}</p>
            </@field.group>
            <@buttons.actionGroup>
                <@buttons.button id="kc-login" name="login" label="waSendCode" class=["kcButtonPrimaryClass", "kcButtonBlockClass"] />
            </@buttons.actionGroup>
        </form>
    <#elseif section = "socialProviders">
        <#if social.providers?? && social.providers?has_content>
            <@identityProviders.show social=social />
        </#if>
    </#if>
</@layout.registrationLayout>
