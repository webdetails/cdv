/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company. All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cdv.api;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.api.resources.PluginResource;
import pt.webdetails.cdv.CdvConstants;
import pt.webdetails.cdv.CdvEngine;
import pt.webdetails.cdv.CdvLifecycleListener;
import pt.webdetails.cdv.DummyHandler;
import pt.webdetails.cdv.Router;
import pt.webdetails.cdv.notifications.NotificationEngine;
import pt.webdetails.cdv.operations.PushWarningsHandler;
import pt.webdetails.cdv.plugin.CdvConfig;
import pt.webdetails.cdv.scripts.GlobalScope;
import pt.webdetails.cdv.util.CdvEnvironment;
import pt.webdetails.cdv.utils.InterPluginBroker;
import pt.webdetails.cdv.utils.JsonUtils;
import pt.webdetails.cdv.utils.RestApiUtils;
import pt.webdetails.cpf.InterPluginCall;
import pt.webdetails.cpf.RestRequestHandler;
import pt.webdetails.cpf.Result;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.VersionChecker;
import pt.webdetails.cpf.persistence.PersistenceEngine;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IBasicFileFilter;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.utils.CharsetHelper;
import pt.webdetails.cpf.utils.MimeTypes;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path( "/cdv/api" )
public class CdvApi {

  static Log logger = LogFactory.getLog( CdvApi.class );

  @GET
  @Path( "/ping" )
  public String ping() {
    logger.info( "pong" );
    return "pong";
  }

  @GET
  @Path( "/hello" )
  public void hello(@Context HttpServletRequest request,
                    @Context HttpServletResponse response ) throws IOException, JSONException {
    route( RestRequestHandler.HttpMethod.GET, "/hello", request, response, null );
  }

  @GET
  @Path( "/warnings" )
  public void warnings(@Context HttpServletRequest request,
                    @Context HttpServletResponse response ) throws IOException, JSONException {
    route( RestRequestHandler.HttpMethod.GET, "/warnings", request, response, null );
  }

  @GET
  @Path( "/refresh" )
  public void refresh() {
    CdvLifecycleListener.reInit();
  }

  @GET
  @Path( "/home" )
  @Produces( MimeTypes.HTML )
  public void home( @Context HttpServletRequest request, @Context HttpServletResponse response,
                    @Context HttpHeaders headers ) {
    new InterPluginCall( InterPluginCall.CDV, "refreshTests" ).call();
    callCDE( "validations.wcdf", request, response, headers, false );
  }

  @GET
  @Path( "/validations" )
  @Produces( MimeTypes.HTML )
  public void validations( @Context HttpServletRequest request, @Context HttpServletResponse response,
                           @Context HttpHeaders headers ) {
    new InterPluginCall( InterPluginCall.CDV, "refreshTests" ).call();
    callCDE( "validations.wcdf", request, response, headers, false );
  }

  @GET
  @Path( "/alerts" )
  @Produces( MimeTypes.HTML )
  public void alerts( @Context HttpServletRequest request, @Context HttpServletResponse response,
                      @Context HttpHeaders headers ) {
    callCDE( "alerts.wcdf", request, response, headers, false );
  }

  @GET
  @Path( "/cdaErrors" )
  @Produces( MimeTypes.HTML )
  public void cdaErrors( @Context HttpServletRequest request, @Context HttpServletResponse response,
                         @Context HttpHeaders headers ) {
    callCDE( "cdaErrors.wcdf", request, response, headers, false );
  }

  @GET
  @Path( "/slowQueries" )
  @Produces( MimeTypes.HTML )
  public void slowQueries( @Context HttpServletRequest request, @Context HttpServletResponse response,
                           @Context HttpHeaders headers ) {
    callCDE( "slowQueries.wcdf", request, response, headers, false );
  }

  @GET
  @Path( "/notificationSettings" )
  @Produces( MimeTypes.HTML )
  public void notificationSettings( @Context HttpServletRequest request, @Context HttpServletResponse response,
                                    @Context HttpHeaders headers ) {
    callCDE( "notificationSettings.wcdf", request, response, headers, false );
  }

  @GET
  @Path( "/about" )
  @Produces( MimeTypes.JSON )
  public void about( @Context HttpServletRequest request, @Context HttpServletResponse response,
                     @Context HttpHeaders headers ) throws IOException, JSONException {
    callCDE( "cdvAbout.wcdf", request, response, headers, true );
  }

  //Endpoints redefined from bootstrap.js

  @GET
  @Path( "/restartTimer" )
  public void restartTimer( @Context HttpServletRequest request,
                            @Context HttpServletResponse response ) throws IOException, JSONException {
    route( RestRequestHandler.HttpMethod.GET, "/restartTimer", request, response, null );
  }

  @GET
  @Path( "/stopTimer" )
  public void stopTimer( @Context HttpServletRequest request,
                         @Context HttpServletResponse response ) throws IOException, JSONException {
    route( RestRequestHandler.HttpMethod.GET, "/stopTimer", request, response, null );
  }

  @GET
  @Path( "/listTests" )
  public void listTests( @Context HttpServletRequest request,
                         @Context HttpServletResponse response ) throws IOException, JSONException {
    route( RestRequestHandler.HttpMethod.GET, "/listTests", request, response, null );
  }

  @GET
  @Path( "/listTestsFlatten" )
  public void listTestsFlatten( @Context HttpServletRequest request,
                                @Context HttpServletResponse response ) throws IOException, JSONException {
    route( RestRequestHandler.HttpMethod.GET, "/listTestsFlatten", request, response, null );
  }

  @GET
  @Path( "/runTests" )
  public void runTests( @Context HttpServletRequest request,
                        @Context HttpServletResponse response ) throws IOException, JSONException {
    route( RestRequestHandler.HttpMethod.GET, "/runTests", request, response, null );
  }

  @GET
  @Path( "/runTest" )
  public void runTest( @Context HttpServletRequest request,
                       @Context HttpServletResponse response) throws IOException, JSONException {
    route( RestRequestHandler.HttpMethod.GET, "/runTest", request, response, null );
  }

  @GET
  @Path( "/refreshNotifications" )
  public void refreshNotifications( @Context HttpServletRequest request,
                                    @Context HttpServletResponse response ) throws IOException, JSONException {
    route( RestRequestHandler.HttpMethod.GET, "/refreshNotifications", request, response, null );
  }

  @GET
  @Path( "/refreshTests" )
  public void refreshTests( @Context HttpServletRequest request,
                            @Context HttpServletResponse response ) throws IOException, JSONException {
    route( RestRequestHandler.HttpMethod.GET, "/refreshTests", request, response, null );
  }

  //Endpoints redefined from queries.js

  @GET
  @Path( "/lastExecution" )
  public void lastExecution( @Context HttpServletRequest request,
                             @Context HttpServletResponse response ) throws IOException, JSONException {
    route( RestRequestHandler.HttpMethod.GET, "/lastExecution", request, response, null );
  }

  @GET
  @Path( "/getAlertsByGroup" )
  public void getAlertsByGroup( @Context HttpServletRequest request,
                                @Context HttpServletResponse response ) throws IOException, JSONException {
    route( RestRequestHandler.HttpMethod.GET, "/getAlertsByGroup", request, response, null );
  }

  @GET
  @Path( "/getAlerts" )
  public void getAlerts( @Context HttpServletRequest request,
                         @Context HttpServletResponse response ) throws IOException, JSONException {
    route( RestRequestHandler.HttpMethod.GET, "/getAlerts", request, response, null );
  }

  @GET
  @Path( "/getLatestResults" )
  public void getLatestResults( @Context HttpServletRequest request,
                                @Context HttpServletResponse response ) throws IOException, JSONException {
    route( RestRequestHandler.HttpMethod.GET, "/getLatestResults", request, response, null );
  }

  @GET
  @Path( "/getCdaErrors" )
  public void getCdaErrors( @Context HttpServletRequest request,
                            @Context HttpServletResponse response ) throws IOException, JSONException {
    route( RestRequestHandler.HttpMethod.GET, "/getCdaErrors", request, response, null );
  }

  @GET
  @Path( "/getCdaSlowQueries" )
  public void getCdaSlowQueries( @Context HttpServletRequest request,
                                 @Context HttpServletResponse response ) throws IOException, JSONException {
    route( RestRequestHandler.HttpMethod.GET, "/getCdaSlowQueries", request, response, null );
  }

  @GET
  @Path( "/deleteCdaEntry" )
  public void deleteCdaEntry( @Context HttpServletRequest request,
                              @Context HttpServletResponse response ) throws IOException, JSONException {
    route( RestRequestHandler.HttpMethod.GET, "/deleteCdaEntry", request, response, null );
  }

  @GET
  @Path( "/deleteCdaEntriesOfEventType" )
  public void deleteCdaEntriesOfEventType( @Context HttpServletRequest request,
                                           @Context HttpServletResponse response ) throws IOException, JSONException {
    route( RestRequestHandler.HttpMethod.GET, "/deleteCdaEntriesOfEventType", request, response, null );
  }

  @GET
  @Path( "/deleteAlert" )
  public void deleteAlert( @Context HttpServletRequest request,
                           @Context HttpServletResponse response ) throws IOException, JSONException {
    route( RestRequestHandler.HttpMethod.GET, "/deleteAlert", request, response, null );
  }

  @GET
  @Path( "/listSchedules" )
  public void listSchedules( @Context HttpServletRequest request,
                             @Context HttpServletResponse response ) throws IOException, JSONException {
    route( RestRequestHandler.HttpMethod.GET, "/listSchedules", request, response, null );
  }

  @GET
  @Path( "/deleteAllAlerts" )
  public void deleteAllAlerts( @Context HttpServletRequest request,
                               @Context HttpServletResponse response ) throws IOException, JSONException {
    route( RestRequestHandler.HttpMethod.GET, "/deleteAllAlerts", request, response, null );
  }

  //Endpoints redefined from selfTests.js

  @GET
  @Path( "/testPersistence" )
  public void testPersistence( @Context HttpServletRequest request,
                               @Context HttpServletResponse response ) throws IOException, JSONException {
    route( RestRequestHandler.HttpMethod.GET, "/testPersistence", request, response, null );
  }

  @GET
  @Path( "/testNotifications" )
  public void testNotifications( @Context HttpServletRequest request,
                                 @Context HttpServletResponse response ) throws IOException, JSONException {
    route( RestRequestHandler.HttpMethod.GET, "/testNotifications", request, response, null );
  }

  //Generic endpoint to get System Resources

  @GET
  @Path( "/{path: [^?]+ }" )
  @Produces( { MediaType.WILDCARD } )
  public Response getSystemResource( @PathParam( "path" ) String path, @Context HttpServletResponse response ) throws
    IOException {

    String pluginId = CdvEngine.getInstance().getEnvironment().getPluginId();

    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );

    if ( !StringUtils.isEmpty( path ) && pluginManager.isPublic( pluginId, path ) ) {

      Response readFileResponse = new PluginResource( response ).readFile( pluginId, path );

      if ( readFileResponse.getStatus() != Response.Status.NOT_FOUND.getStatusCode() ) {
        return readFileResponse;
      }
    }

    return Response.status( Response.Status.NOT_FOUND ).build();
  }


  @GET
  @Path( "/newTest" )
  @Produces( MimeTypes.JSON )
  public String newTest( @QueryParam( MethodParams.NEW_NAME ) String newName ) throws IOException, JSONException {
    JSONObject result = createTest("", newName ); //"system/cdv/validationTemplate.cdv",
    return result.toString( 2 );
  }

  @GET
  @Path( "/duplicateTest" )
  @Produces( MimeTypes.JSON )
  public String duplicateTest( @QueryParam( MethodParams.NEW_NAME ) String newName,
                               @QueryParam( MethodParams.PATH ) String path ) throws IOException, JSONException {
    JSONObject result;

    //Need to validate this a bit more - ensure that path is in cdv/tests at least
    if ( path != null ) {
      result = createTest(path, newName );        //path,
    } else {
      result = new JSONObject();
      result.put( "success", "false" );
    }

    return result.toString( 2 );
  }

  @GET
  @Path( "/copyCDVTests" )
  @Produces( MimeTypes.JSON )
  public String copyCDVTests() throws IOException, JSONException {
    JSONObject result = copyCDVTestsFromPluginSamples();
    return result.toString( 2 );
  }

  @GET
  @Path( "/deleteTable" )
  @Produces( MimeTypes.JSON )
  public String deleteTable( @QueryParam( MethodParams.CLASS ) String classTable ) throws IOException, JSONException {
    if ( classTable != null ) {
      int deleted = PersistenceEngine.getInstance().deleteAll( classTable );
      JSONObject result = Result.getOK( "deleted " + deleted + " instances" ).toJSON();
      return result.toString( 2 );
    } else {
      return Result.getError( "No class" ).toJSON().toString( 2 );
    }
  }

  @GET
  @Path( "/listTable" )
  @Produces( MimeTypes.JSON )
  public String listTable( @QueryParam( MethodParams.CLASS ) String classTable ) throws IOException, JSONException {
    if ( classTable != null ) {
      JSONObject result = PushWarningsHandler.listClass( classTable ).toJSON();
      return result.toString( 2 );
    } else {
      return Result.getError( "No class" ).toJSON().toString( 2 );
    }
  }

  @GET
  @Path( "/listCda" )
  @Produces( MimeTypes.JSON )
  public String listCda( @QueryParam( MethodParams.DATA_ACCESS_ID ) String dataAccessId,
                         @QueryParam( MethodParams.CDA_SETTINGS_ID ) String settingsId )
    throws IOException, JSONException {
    if ( !StringUtils.isEmpty( dataAccessId ) && !StringUtils.isEmpty( settingsId ) ) {
      JSONObject result = PushWarningsHandler.listClass( settingsId, dataAccessId ).toJSON();
      return result.toString( 2 );
    } else {
      return Result.getError( "No dataAccessId or empty, No cdaSettingsId or empty" ).toJSON().toString( 2 );
    }
  }

  @GET
  @Path( "/checkVersion" )
  @Produces( MimeTypes.JSON )
  public void checkVersion( @Context HttpServletResponse response ) throws IOException, JSONException {
    JSONObject result = getVersionChecker().checkVersion().toJSON();
    JsonUtils.buildJsonResult( response.getOutputStream(), result != null, result );
  }

  @GET
  @Path( "/getVersion" )
  @Produces( MimeTypes.PLAIN_TEXT)
  public String getVersion( @Context HttpServletResponse response ) throws IOException, JSONException {
    return  getVersionChecker().getVersion();
  }

  public VersionChecker getVersionChecker() {
    return new VersionChecker( CdvConfig.getConfig() ) {

      @Override
      protected String getVersionCheckUrl( VersionChecker.Branch branch ) {
        switch( branch ) {
          case TRUNK:
            return "http://ci.analytical-labs.com/job/Webdetails-CDV/lastSuccessfulBuild/artifact/dist/marketplace.xml";
          case STABLE:
            return "http://ci.analytical-labs" +
              ".com/job/Webdetails-CDV-Release/lastSuccessfulBuild/artifact/dist/marketplace.xml";
          default:
            return null;
        }
      }
    };
  }

  private JSONObject createTest(String origin, String newPath ) throws JSONException {
    JSONObject result = new JSONObject();
    if ( !newPath.endsWith( ".cdv" ) ) {
      newPath = newPath + ".cdv";
    }
    String newFileName =
      Util.joinPath( CdvEnvironment.getPluginRepositoryDir(), CdvConstants.SolutionFolders.TESTS, newPath );

    IRWAccess pluginRepo = CdvEnvironment.getPluginRepositoryWriter( "tests" );
    IReadAccess systemRepo = CdvEnvironment.getPluginSystemReader();
    IReadAccess contentRepo = CdvEnvironment.getUserContentAccess();

    try {
      if ( pluginRepo.fileExists( newPath ) ) {
        logger.error( "New File already exists, aborting creation of new test" );
        result.put( "success", "false" );
        return result;
      }

      InputStream inputStream;

      if (StringUtils.isEmpty( origin )) {
        inputStream = systemRepo.getFileInputStream( "validationTemplate.cdv" );
      }
      else {
        inputStream = contentRepo.getFileInputStream( origin );
      }

      StringWriter writer = new StringWriter();
      IOUtils.copy( inputStream, writer, CharsetHelper.getEncoding() );
      String originalTest = writer.toString();


      originalTest = Pattern.compile( "path:\\s*['\"].*['\"]\\s*," ).matcher( originalTest )
        .replaceFirst( "path: '" + newFileName + "'," );
      originalTest = Pattern.compile( "name:\\s*['\"].*['\"]\\s*," ).matcher( originalTest ).replaceFirst(
        "name: '" + newFileName.substring( newFileName.lastIndexOf( "/" ) + 1 )
          .replaceAll( Matcher.quoteReplacement( ".cdv" ), "" ) + "'," );
      originalTest = Pattern.compile( "createdBy:\\s*['\"].*['\"]\\s*," ).matcher( originalTest )
        .replaceFirst( "createdBy: '" + PentahoSessionHolder.getSession().getName() + "'," );

      inputStream = new ByteArrayInputStream( originalTest.getBytes( CharsetHelper.getEncoding() ) );

      pluginRepo.saveFile( newPath, inputStream );
      result.put( "success", "true" );
      result.put( "path", newFileName );
      InterPluginCall pluginCall = new InterPluginCall( InterPluginCall.CDV, "refreshTests" );
      pluginCall.call();
    } catch ( IOException ioe ) {
      logger.error( "Error while creating test file", ioe );
      result.put( "success", "false" );
    }

    return result;
  }

  private JSONObject copyCDVTestsFromPluginSamples() throws JSONException, IOException {
    JSONObject result = new JSONObject();

    IBasicFileFilter cdvFilter = new IBasicFileFilter() {
      public boolean accept( IBasicFile file ) {
        return file.getExtension().equals( "cdv" );
      }
    };

    IReadAccess repAccess = CdvEnvironment.getUserContentAccess();

    IReadAccess readSystemSamples = CdvEnvironment.getPluginSystemReader( "sampleFiles" );
    IRWAccess writeSamples = CdvEnvironment.getPluginRepositoryWriter( "tests" );

    List<IBasicFile> tests = readSystemSamples.listFiles( "", cdvFilter );
    for ( IBasicFile test : tests ) {
      InputStream stream = readSystemSamples.getFileInputStream( test.getName() );
      writeSamples.saveFile( test.getName(), stream );
    }

    tests = repAccess.listFiles( "public/plugin-samples/cdv", cdvFilter );
    for ( IBasicFile test : tests ) {
      InputStream stream = repAccess.getFileInputStream( Util.joinPath( "public/plugin-samples/cdv", test.getName() ) );
      writeSamples.saveFile( test.getName(), stream );
    }

    InterPluginCall pluginCall = new InterPluginCall( InterPluginCall.CDV, "refreshTests" );
    pluginCall.call();

    result.put( "success", "true" );
    return result;
  }

  private void callCDE( String file, HttpServletRequest request, HttpServletResponse response,
                        HttpHeaders headers, boolean requestRoot ) {

    Map<String, Map<String, Object>> params = RestApiUtils.buildBloatedMap( request, response, headers );
    Map<String, Object> requestMap = new HashMap<String, Object>();
    Map<String, Object> requestParams = params.get( "request" );

    requestMap.put( "solution", "system" );
    requestMap.put( "path", "cdv/presentation/" );
    requestMap.put( "file", file );
    requestMap.put( "absolute", "false" );
    requestMap.put( "inferScheme", "false" );
    if ( requestRoot ) {
      requestMap.put( "root", getRoot( request ) );
    }

    for ( String name : requestParams.keySet() ) {
      requestMap.put( name, requestParams.get( name ) );
    }

    if ( requestMap.get( "mode" ) != null && requestMap.get( "mode" ).equals( "edit" ) ) {
      try {
        redirectToCdeEditor( response, requestMap );
      } catch ( IOException ex ) {
        logger.error( "Couldn't redirect to cde." );
      }
      return;
    }

    try {
      InterPluginBroker.run( requestMap, response.getOutputStream() );
    } catch ( Exception e ) {
      logger.error( "Couldn't run cde." );
    }
  }

  private void redirectToCdeEditor( HttpServletResponse response,
                                    Map<String, Object> params ) throws IOException {

    StringBuilder urlBuilder = new StringBuilder();
    urlBuilder.append( "../../pentaho-cdf-dd/api/renderer/edit" );
    if ( params.size() > 0 ) {
      urlBuilder.append( "?" );
    }

    List<String> paramArray = new ArrayList<String>();
    for ( String key : params.keySet() ) {
      Object value = params.get( key );
      if ( value instanceof String ) {
        paramArray.add( key + "=" + URLEncoder.encode( (String) value, CharsetHelper.getEncoding() ) );
      }
    }

    urlBuilder.append( StringUtils.join( paramArray, "&" ) );

    if ( response == null ) {
      logger.error( "response not found" );
      return;
    }
    try {
      response.sendRedirect( urlBuilder.toString() );
    } catch ( IOException e ) {
      logger.error( "could not redirect", e );
    }
  }

  private void route( RestRequestHandler.HttpMethod httpMethod, String path,
                      HttpServletRequest request, HttpServletResponse response, HttpHeaders headers )
    throws IOException {

    if ( Router.getBaseRouter().canHandle( httpMethod, path ) ) {
      Map<String, Map<String, Object>> params = RestApiUtils.buildBloatedMap( request, response, headers );

      Router.getBaseRouter().route( httpMethod, path, response.getOutputStream(),
        RestApiUtils.getPathCommonParameters( params ) ,
        RestApiUtils.getRequestCommonParameters( params ) );
      response.getOutputStream().flush();
    }
  }

  private String getRoot( ServletRequest wrapper ) {
    return wrapper.getScheme() + "://" + wrapper.getServerName() + ":" + wrapper.getServerPort();
  }

  private class MethodParams {
    public static final String NEW_NAME = "newName";
    public static final String PATH = "path";
    public static final String CLASS = "class";
    public static final String DATA_ACCESS_ID = "dataAccessId";
    public static final String CDA_SETTINGS_ID = "cdaSettingsId";
  }
}
