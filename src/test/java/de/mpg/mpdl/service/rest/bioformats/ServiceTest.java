package de.mpg.mpdl.service.rest.bioformats;

import de.mpg.mpdl.service.rest.bioformats.ServiceConfiguration.Pathes;
import org.glassfish.jersey.jsonp.JsonProcessingFeature;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class ServiceTest extends JerseyTest
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceTest.class);
    static FormDataMultiPart SWC_MULTIPART = null;
    static String SWC_CONTENT = null;
    static String SWC_URL = null;

    final static String FORMATS_XML_FILE = "formats.xml";
    final static String BAD_INPUT_FORMAT_FILE = "bad-input-format-sample.swc";
    final static String GOOD_INPUT_FORMAT_FILE = "m42_40min_red.fits";
    final static String TEST_INPUT_FILE = "2lbrianlaiphot_BMP.bmp";
    final static String GOOD_OUTPUT_FILE = "m42_40min_red.png";
    final static int EXPECTED_RESPONSE_SIZE = 9931709;

    static String FORMATS_XML = null;

    final static MediaType PNG_MEDIA_TYPE = new MediaType("image", "png");


    @Override
    protected Application configure() {
        return new MyApplication();
    }


    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new MyTestContainerFactory();
    }

    @BeforeClass
    public static void initilizeResources() {
    	  URI uri = null;
    	  FileDataBodyPart filePart = null;
        try {
            FORMATS_XML = RestUtils.getResourceAsString(FORMATS_XML_FILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
      
        try {
            SWC_CONTENT = RestUtils.getResourceAsString(GOOD_INPUT_FORMAT_FILE);
            uri = RestUtils.getResourceAsURL(GOOD_OUTPUT_FILE).toURI();
            SWC_URL = uri.toURL().toString();
            filePart = new FileDataBodyPart("file1", new File(uri));
            SWC_MULTIPART = new FormDataMultiPart();
            SWC_MULTIPART.bodyPart(filePart);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testViewFromFile() throws Exception {
    	  
    	
    	   Response response = target(Pathes.PATH_CONVERT)
                   .register(MultiPartFeature.class)
                   .request(MediaType.MULTIPART_FORM_DATA_TYPE, MediaType.TEXT_HTML_TYPE)
                   .post(Entity.entity(SWC_MULTIPART, SWC_MULTIPART.getMediaType()));
            assertThat(response.readEntity(String.class), not(isEmptyOrNullString()));

    }

    @Test
    public void testViewFromUrl() throws Exception {

    }

    @Test
    public void testFormats() throws Exception {

        Response response = target(Pathes.PATH_FORMATS)
                .request(MediaType.TEXT_XML)
                .get();

        assertEquals(200, response.getStatus());
        String responseXml = response.readEntity(String.class);
        assertThat(responseXml, not(isEmptyOrNullString()));
        assertEquals(responseXml, FORMATS_XML);
    }

    //@Test
    public void testUnsupportedInputFileFormat() throws IOException, URISyntaxException {

        FormDataMultiPart multipart = getFormDataMultiPart(BAD_INPUT_FORMAT_FILE);

        Response response = target(Pathes.PATH_CONVERT)
                .register(MultiPartFeature.class)
                .register(JsonProcessingFeature.class)
                .request(MediaType.MULTIPART_FORM_DATA_TYPE, MediaType.APPLICATION_JSON_TYPE, MediaType.TEXT_HTML_TYPE)
                .post(Entity.entity(multipart, multipart.getMediaType()));

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        JsonObject responseJSON = response.readEntity(JsonObject.class);
        assertNotNull(responseJSON);

        JsonObject jsonObject = (JsonObject) responseJSON.get("error");

        assertEquals(RestUtils.UNSUPPORTED_FILE_FORMAT_CUSTOM_CODE, jsonObject.getString("code"));

    }

    @Test
    public void testSupportedInputFileFormat() throws IOException, URISyntaxException {

        FormDataMultiPart multipart = getFormDataMultiPart(GOOD_INPUT_FORMAT_FILE);

        Response response = target(Pathes.PATH_CONVERT)
                .register(MultiPartFeature.class)
                .register(JsonProcessingFeature.class)
                .request(MediaType.MULTIPART_FORM_DATA_TYPE, MediaType.APPLICATION_JSON_TYPE,MediaType.TEXT_HTML_TYPE )
                .post(Entity.entity(multipart, multipart.getMediaType()));

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        String responseStr = response.readEntity(String.class);
        assertThat("Wrong size of generated png file:", responseStr.length(), equalTo(EXPECTED_RESPONSE_SIZE));


    }

    private FormDataMultiPart getFormDataMultiPart(String fileName) throws URISyntaxException, IOException {
        URI uri = RestUtils.getResourceAsURL(GOOD_INPUT_FORMAT_FILE).toURI();
        FileDataBodyPart filePart = new FileDataBodyPart("file", new File(uri));
        assertNotNull(filePart);
        FormDataMultiPart multipart = new FormDataMultiPart();
        multipart.bodyPart(filePart);
        return multipart;
    }
    
    private void testFile(FormDataMultiPart multipart, String path, MediaType responseMediaType) {


        Response response = target(path)
                .register(MultiPartFeature.class)
                .request(MediaType.MULTIPART_FORM_DATA_TYPE)
                .accept(responseMediaType)
                .post(Entity.entity(multipart, multipart.getMediaType()));

        assertEquals(200, response.getStatus());
        assertThat(response.readEntity(String.class), not(isEmptyOrNullString()));

    }


}
