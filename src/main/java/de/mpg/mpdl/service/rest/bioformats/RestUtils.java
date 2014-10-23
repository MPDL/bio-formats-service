package de.mpg.mpdl.service.rest.bioformats;

import com.google.common.io.CharStreams;
import com.google.common.io.Closer;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created by vlad on 10/6/14.
 */
public class RestUtils {


    private static final Logger LOGGER = LoggerFactory.getLogger(RestUtils.class);

    private static final String JAVAX_SERVLET_CONTEXT_TEMPDIR = "javax.servlet.context.tempdir";

    public final static String FORMATS_XML_FILE = "formats.xml";

    public static final String UNSUPPORTED_FILE_FORMAT_CUSTOM_CODE = "1415";


    public static Response generateFormatList() throws IOException {
        return buildXmlResponse(
                getResourceAsString(FORMATS_XML_FILE), Status.OK
        );
    }

    public static Response buildXmlResponse(String str, Status status) {
        return Response.status(status).entity(str).type(MediaType.TEXT_XML)
                .build();
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


        return Response
                .status(Status.OK)
                .entity(getResourceAsInputStream("m42_40min_red.png"))
                .type("image/png")
                .build();

/*
        return Response
                .status(Status.OK)
                .entity(generateUnsupportedInputFileFormatJSON(fileItem.getName()))
                .type(MediaType.APPLICATION_JSON)
                .build();
*/
    }


// Helpers

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
