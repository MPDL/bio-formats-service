package de.mpg.mpdl.service.rest.bioformats;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.DatatypeConverter;

import loci.formats.FormatException;
import loci.formats.IFormatWriter;
import loci.formats.ImageWriter;
import loci.formats.in.ImaconReader;







import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.internal.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.core.pattern.Converter;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.Closer;

/**
 * Created by vlad on 10/6/14.
 */
public class RestUtils {


    private static final Logger LOGGER = LoggerFactory.getLogger(RestUtils.class);

    private static final String JAVAX_SERVLET_CONTEXT_TEMPDIR = "javax.servlet.context.tempdir";

    public final static String FORMATS_XML_FILE = "formats.xml";

    public static final String UNSUPPORTED_FILE_FORMAT_CUSTOM_CODE = "1415";
    
    public static final String BIO_FORMATS_VIEW_HTML_TEMPLATE_FILE_NAME = "bio_formats_template.html";

    private static ImageConverter converter;
    
    public static File TMP_DIRECTORY = null;
    


    public static Response generateFormatList() throws IOException {
        return buildXmlResponse(
                getResourceAsString(FORMATS_XML_FILE), Status.OK
        );
    }

    public static Response buildXmlResponse(String str, Status status) {
        return Response.status(status).entity(str).type(MediaType.TEXT_XML)
                .build();
    }
    
    public static Response generateViewFromFiles(HttpServletRequest request)throws Exception{
    	return convertFile(request);
    }
    
    public static Response generateViewFromUrl(String url) throws Exception{
    	URLConnection bioFormatsSourceConnection =  URI.create(url).toURL().openConnection();
    	return generateConverterImage(bioFormatsSourceConnection.getInputStream());
    }
    

    public static String getResourceAsString(String fileName) throws IOException {
        return getInputStreamAsString(getResourceAsInputStream(fileName));
    }

    private static String getInputStreamAsString(InputStream stream)
            throws IOException {
        Closer closer = Closer.create();
        closer.register(stream);
        String string = null;
        try {
            string = CharStreams.toString(new InputStreamReader(stream,
                    StandardCharsets.UTF_8));
        } catch (Throwable e) {
            closer.rethrow(e);
        } finally {
            closer.close();
        }
        return string;
    }
    
    public static InputStream getResourceAsInputStream(String fileName)
            throws IOException {
        return new RestUtils().getClass().getClassLoader()
                .getResourceAsStream(fileName);
    }

    public static URL getResourceAsURL(String fileName)
            throws IOException {
        return new RestUtils().getClass().getClassLoader()
                .getResource(fileName);
    }
    
    public static Response convertFile(HttpServletRequest request)
            throws Exception {

        List<FileItem> fileItems = uploadFiles(request);
        FileItem fileItem = getFirstFileItem(fileItems);


        return generateConverterImage(fileItem);


    }
 
 public static Response generateConverterImage(FileItem fileItem) throws Exception{
	 
	 File tempInputFile = File.createTempFile("input", null, TMP_DIRECTORY);

	 fileItem.write(tempInputFile);
	 
	 String[] args = new String[2];
	 args[0] = tempInputFile.getAbsolutePath();
	 
	 args[1] = args[0].substring(0,args[0].length()-4) +".png";

	 converter = new ImageConverter();
	 if(converter.testConvert(new ImageWriter(), args)){
		 File outputFile = new File(args[1]);
		 return buildHtmlResponse(generateResponseHtml(outputFile));
	 }return buildHtmlResponse("Error", Status.UNSUPPORTED_MEDIA_TYPE);
	 
 }
 
 public static Response generateConverterImage(InputStream stream) throws Exception{
	 
	 File tempInputFile = File.createTempFile("input", null);
	 OutputStream outputStream = new FileOutputStream(tempInputFile);
	 int read = 0;
	 byte[] bytes = new byte[1024];
	 while((read = stream.read(bytes)) != -1){
		 outputStream.write(bytes, 0, read);
	 }
	 stream.close();
	 outputStream.close();
	 
	 String[] args = new String[2];
	 args[0] = tempInputFile.getAbsolutePath();
	 
	 args[1] = args[0].substring(0,args[0].length()-4) +".png";

	 converter = new ImageConverter();
	 if(converter.testConvert(new ImageWriter(), args)){
		 File outputFile = new File(args[1]);
		 return buildHtmlResponse(generateResponseHtml(outputFile));
	 }return buildHtmlResponse("Error", Status.UNSUPPORTED_MEDIA_TYPE);
	 
 }
 
 public static Response generateConverterImage(File file) throws Exception{
	 
	 File tempOutputFile = File.createTempFile("out", ".png");
	 tempOutputFile.deleteOnExit();
	 String[] args = new String[2];
	 args[0] = file.getAbsolutePath();
	 args[1] = tempOutputFile.getAbsolutePath();
	 converter = new ImageConverter();
	 if(converter.testConvert(new ImageWriter(), args)){
		 return buildHtmlResponse(generateResponseHtml(tempOutputFile));
	 }return buildHtmlResponse("Error", Status.UNSUPPORTED_MEDIA_TYPE);
	 
 }
    
 public static String generateResponseHtml(byte [] png) throws Exception{
	 	String png_base64Code = encodeToString(png);

    	String chunk = getResourceAsString(BIO_FORMATS_VIEW_HTML_TEMPLATE_FILE_NAME);
    	return chunk.replace("%PNG_PLACEHOLDER%", png_base64Code);
    }
 
 public static String generateResponseHtml(File png) throws Exception{
	 	String png_base64Code = encodeFileToString(png);
	 
 	String chunk = getResourceAsString(BIO_FORMATS_VIEW_HTML_TEMPLATE_FILE_NAME);
 	return chunk.replace("%PNG_PLACEHOLDER%", png_base64Code);
 }
 
    
 public static String encodeFileToString(File png) throws Exception{
	String base64Code = null;
	base64Code = DatatypeConverter.printBase64Binary(FileUtils.readFileToByteArray(png));
	return base64Code;
 }
 
 public static String encodeImageToString(BufferedImage image, String type) throws Exception{
	String base64Code = null;
	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	ImageIO.write(image, type, bos);
	byte[] imageBytes = bos.toByteArray();
	base64Code = DatatypeConverter.printBase64Binary(imageBytes);
	bos.close();
	return base64Code;
 }
 
 public static String encodeToString(byte [] imageByteArray) throws Exception{
	 String base64Code = null;
//	 base64Code = Base64.encodeAsString(imageByteArray);
	 base64Code = DatatypeConverter.printBase64Binary(imageByteArray);
	 return base64Code;
 }

// Helpers
    public static Response buildHtmlResponse(String str) {
		return buildHtmlResponse(str, Status.OK);
	}
    

	public static Response buildHtmlResponse(String str, Status status) {
		return Response.status(status).entity(str).type(MediaType.TEXT_HTML)
				.build();
	}


    private static JsonObject generateUnsupportedInputFileFormatJSON(String fileName) {

        return Json.createObjectBuilder()
                .add("error",
                    Json.createObjectBuilder()
                    .add("code", UNSUPPORTED_FILE_FORMAT_CUSTOM_CODE)
                    .add("field", "file")
                    .add("message", "unsupported-input-file-format")
                    .add("description", "Unsupported input format of file: " + fileName)
                )
                .build();
    }

    public static List<FileItem> uploadFiles(HttpServletRequest request)
            throws FileUploadException {
        List<FileItem> items = null;
        if (ServletFileUpload.isMultipartContent(request)) {
            ServletContext servletContext = request.getServletContext();
            File repository = (File) servletContext
                    .getAttribute(JAVAX_SERVLET_CONTEXT_TEMPDIR);
            TMP_DIRECTORY = repository;
            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setRepository(repository);
            ServletFileUpload fileUpload = new ServletFileUpload(factory);
            items = fileUpload.parseRequest(request);
        }
        return items;
    }

    // get only first processed file!
    public static FileItem getFirstFileItem(List<FileItem> fileItems)
            throws IOException {

        if (LOGGER.isDebugEnabled()) {
            for (FileItem fileItem : fileItems) {
                if (fileItem.isFormField()) {
                    LOGGER.debug("fileItem.getFieldName():"
                            + fileItem.getFieldName());
                    LOGGER.debug("value:" + fileItem.getString());
                }
            }
        }

        for (FileItem fileItem : fileItems) {
            if (!fileItem.isFormField()) {
                return fileItem;
            }
        }
        return null;
    }
}
