package com.zenuml.confluence.macro;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.AttachmentManager;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.text.StringEscapeUtils;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@Scanned
public class SequenceMacro implements Macro {

    private PageBuilderService pageBuilderService;
    private AttachmentManager attachmentManager;
    private SettingsManager settingsManager;

    @Autowired
    public SequenceMacro(
            @ComponentImport PageBuilderService pageBuilderService,
            @ComponentImport AttachmentManager attachmentManager,
            @ComponentImport SettingsManager settingsManager
    ) {
        this.pageBuilderService = pageBuilderService;
        this.attachmentManager = attachmentManager;
        this.settingsManager = settingsManager;
    }

    private String createMD5(String plaintext) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            return DatatypeConverter.printHexBinary(digest).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String getPDFExportImgTag(String dsl, ConversionContext conversionContext) {
        String baseUrl = settingsManager.getGlobalSettings().getBaseUrl();
        String hash = createMD5(dsl);
        Attachment attachment = attachmentManager.getAttachment(conversionContext.getEntity(),"zenuml-"+hash);
        if(attachment==null) {
            throw new RuntimeException("Error: Attachment is not found");
        }
        return String.join("", "<img src=\""+baseUrl+attachment.getDownloadPath()+"\"/>");
    }

    public String execute(Map<String, String> map, String s, ConversionContext conversionContext) {
        try {
            pageBuilderService.assembler().resources().requireWebResource("com.zenuml.confluence-addon:active-sequence-resources");
            String outputType = conversionContext.getOutputType();
            if (outputType.equalsIgnoreCase("pdf")) {
                return getPDFExportImgTag(s, conversionContext);
            }
            String escapedHTML = StringEscapeUtils.escapeHtml4(s);
            return String.join("", "<diagram-as-code>", escapedHTML, "</diagram-as-code>");
        } catch (RuntimeException e){
            return "<div> We are not able to render the macro. Please contact the support at https://zenuml.atlassian.net/servicedesk </div>";
        }
    }

    public BodyType getBodyType() { return BodyType.PLAIN_TEXT; }

    public OutputType getOutputType() { return OutputType.BLOCK; }

}
