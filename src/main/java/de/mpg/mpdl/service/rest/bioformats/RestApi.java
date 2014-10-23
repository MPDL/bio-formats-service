package de.mpg.mpdl.service.rest.bioformats;

import de.mpg.mpdl.service.rest.bioformats.ServiceConfiguration.Pathes;

import org.apache.commons.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import java.io.IOException;

import javax.ws.rs.core.MediaType;

//import javax.inject.Singleton;

//@Singleton
@Path("/")
public class RestApi {

	private static final Logger LOGGER = LoggerFactory.getLogger(RestApi.class);


	/**
	 * The static explain is resolved by UrlRewriteRule
	 * 
	 * @GET @Path(Pathes.PATH_EXPLAIServiceConfigurationN)
	 * @Produces(MediaType.TEXT_HTML) public Response getExplain() { return
	 *                                RestProcessUtils.getExplain(); }
	 */


    @GET
    @Path(Pathes.PATH_FORMATS)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_XML)
    public Response getFormats()
            throws IOException {
        return RestUtils.generateFormatList();

    }

    @POST
	@Path(Pathes.PATH_CONVERT)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({"image/png", MediaType.TEXT_HTML})
	public Response convertFile(@Context HttpServletRequest request
	) throws Exception {
//        return RestUtils.convertFile(request);
    	return RestUtils.generateViewFromFiles(request);
	}
    
    @GET
	@Path(Pathes.PATH_CONVERT)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({"image/png", MediaType.TEXT_HTML})
	public Response getViewFromUrl(@QueryParam("url") String url)
			throws Exception {
    	System.out.println("url:"+ url);
        return RestUtils.generateViewFromUrl(url);

	}

	/*@POST
	@Path(Pathes.PATH_CONVERT)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	public Response getViewFromFiles(@Context HttpServletRequest request
	) throws IOException, FileUploadException {
        return RestProcessUtils.generateViewFromFiles(request);
	}

	@POST
	@Path(Pathes.PATH_CONVERT)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	public Response getViewFromTextarea(@FormParam("swc") String swc,
			@DefaultValue("false") @FormParam("portable") boolean portable)
			throws IOException {
		return RestProcessUtils.generateViewFromTextarea(swc, portable);
	}

	*//*
	 * /view{?portable=true}
	 *//*
	@GET
	@Path(Pathes.PATH_CONVERT)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	public Response getViewFromUrl(@QueryParam("url") String url,
			@DefaultValue("false") @QueryParam("portable") boolean portable)
			throws IOException {
        return RestProcessUtils.generateViewFromUrl(url, portable);

	}

	@POST
	@Path(Pathes.PATH_THUMB)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces("image/png")
	public Response getThumbnailFromTextarea(@FormParam("swc") String swc)
			throws IOException {
		return RestProcessUtils.generateThumbnailFromTextarea(swc);
	}

	@POST
	@Path(Pathes.PATH_THUMB)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("image/png")
	public Response getThumbnailFromFiles(@Context HttpServletRequest request)
			throws IOException, FileUploadException {
		return RestProcessUtils.generateThumbnailFromFiles(request);
	}

	@GET
	@Path(Pathes.PATH_THUMB)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces("image/png")
	public Response getThumbnailFromUrl(@QueryParam("url") String url)
			throws IOException {
		return RestProcessUtils.generateThumbnailFromUrl(url);
	}

	@POST
	@Path(Pathes.PATH_ANALYZE)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAnalyzeFromTextarea(@FormParam("swc") String swc,
			@FormParam("numberOfBins") String numberOfBins,
			@FormParam("typeOfBins") String typeOfBins,
			@FormParam("query") String query) throws IOException {
		return RestProcessUtils.generateAnalyzeFromTextArea(swc, query, Integer
				.parseInt(numberOfBins), "width".equals(typeOfBins));
	}

	@POST
	@Path(Pathes.PATH_ANALYZE)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAnalyzeFromFiles(@Context HttpServletRequest request)
			throws IOException, FileUploadException {
		return RestProcessUtils.generateAnalyzeFromFiles(request);
	}

	@GET
	@Path(Pathes.PATH_ANALYZE)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAnalyzeFromUrl(@QueryParam("url") String url,
			@QueryParam("numberOfBins") String numberOfBins,
			@QueryParam("typeOfBins") String typeOfBins,
			@QueryParam("query") String query) throws IOException {
		return RestProcessUtils.generateAnalyzeFromUrl(url, query, Integer
				.parseInt(numberOfBins), "width".equals(typeOfBins));
	}*/
}
