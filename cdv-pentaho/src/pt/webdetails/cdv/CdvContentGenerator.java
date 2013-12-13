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

package pt.webdetails.cdv;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import pt.webdetails.cdv.operations.PushWarningsHandler;
import pt.webdetails.cdv.plugin.CdvConfig;
import pt.webdetails.cdv.util.CdvEnvironment;
import pt.webdetails.cpf.InterPluginCall;
import pt.webdetails.cpf.RestRequestHandler;
import pt.webdetails.cpf.Result;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.VersionChecker;
import pt.webdetails.cpf.WrapperUtils;
import pt.webdetails.cpf.annotations.AccessLevel;
import pt.webdetails.cpf.annotations.Exposed;
import pt.webdetails.cpf.persistence.PersistenceEngine;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IBasicFileFilter;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;
import pt.webdetails.cpf.utils.CharsetHelper;
import pt.webdetails.cpf.utils.MimeTypes;
import pt.webdetails.cpf.utils.PluginUtils;

import javax.servlet.ServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CdvContentGenerator extends RestContentGenerator {

  private static final long serialVersionUID = 1L;
  public static final String CDW_EXTENSION = ".cdw";
  public static final String PLUGIN_NAME = "cdv";
  public static final String PLUGIN_PATH = "system/" + CdvContentGenerator.PLUGIN_NAME + "/";
  private static final String UI_PATH = "cdv/presentation/";

  @Override
  public String getPluginName() {
    return PLUGIN_NAME;
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void refresh( OutputStream out ) {
    CdvLifecycleListener.reInit();
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void home( OutputStream out ) throws UnsupportedEncodingException, IOException {
    new InterPluginCall( InterPluginCall.CDV, "refreshTests" ).call();
    callCDE( "validations.wcdf", out );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void validations( OutputStream out ) throws UnsupportedEncodingException, IOException {
    new InterPluginCall( InterPluginCall.CDV, "refreshTests" ).call();
    callCDE( "validations.wcdf", out );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void alerts( OutputStream out ) throws UnsupportedEncodingException, IOException {
    callCDE( "alerts.wcdf", out );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void cdaErrors( OutputStream out ) throws UnsupportedEncodingException, IOException {
    callCDE( "cdaErrors.wcdf", out );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void slowQueries( OutputStream out ) throws UnsupportedEncodingException, IOException {
    callCDE( "slowQueries.wcdf", out );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void notificationSettings( OutputStream out ) throws UnsupportedEncodingException, IOException {
    callCDE( "notificationSettings.wcdf", out );
  }

  private JSONObject createTest( String origin, String newPath ) throws JSONException {
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
      } else {
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
    } catch ( IOException ioe ) {
      logger.error( "Error while creating test file", ioe );
      result.put( "success", "false" );
    }

    return result;
  }

  private JSONObject copyCDVTestsFromPluginSamples() throws JSONException {
    JSONObject result = new JSONObject();

    IBasicFileFilter cdvFilter = new IBasicFileFilter() {
      public boolean accept( IBasicFile file ) {
        return file.getExtension().equals( "cdv" );
      }
    };

    IUserContentAccess repAccess = CdvEnvironment.getUserContentAccess();

    final List<String> origins = new ArrayList<String>();
    origins.add( "plugin-samples/cdv/" );
    origins.add( "system/cdv/sampleFiles/" );
    final String destination = "cdv/tests/";

    if ( repAccess.fileExists( destination ) ) {
      for ( String origin : origins ) {
        List<IBasicFile> tests = repAccess.listFiles( origin, cdvFilter );
        for ( IBasicFile test : tests ) {
          repAccess.copyFile( test.getPath(), destination + test.getName() );
        }
      }
      InterPluginCall pluginCall = new InterPluginCall( InterPluginCall.CDV, "refreshTests" );
      pluginCall.call();
    } else {
      result.put( "success", "false" );
      return result;
    }
    result.put( "success", "true" );
    return result;
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC, outputType = MimeTypes.JSON )
  public void newTest( OutputStream out ) throws IOException, JSONException {
    String newName = getRequestParameters().getStringParameter( "newName", null );
    JSONObject result = createTest( "system/cdv/validationTemplate.cdv", newName );
    writeOut( out, result.toString( 2 ) );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC, outputType = MimeTypes.JSON )
  public void duplicateTest( OutputStream out ) throws IOException, JSONException {
    String path = getRequestParameters().getStringParameter( "path", null );
    String newName = getRequestParameters().getStringParameter( "newName", null );
    JSONObject result;

    //Need to validate this a bit more - ensure that path is in cdv/tests at least
    if ( path != null ) {
      result = createTest( path, newName );
    } else {
      result = new JSONObject();
      result.put( "success", "false" );
    }
    writeOut( out, result.toString( 2 ) );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC, outputType = MimeTypes.JSON )
  public void copyCDVTests( OutputStream out ) throws IOException, JSONException {
    JSONObject result;

    result = copyCDVTestsFromPluginSamples();
    writeOut( out, result.toString( 2 ) );
  }

  //TODO: TEMP!
  @Exposed( accessLevel = AccessLevel.ADMIN, outputType = MimeTypes.PLAIN_TEXT )
  public void deleteTable( OutputStream out ) throws IOException {
    String classTable = getRequestParameters().getStringParameter( "class", null );
    if ( classTable != null ) {
      int deleted = PersistenceEngine.getInstance().deleteAll( classTable );
      writeOut( out, "deleted " + deleted + " instances" );
    } else {
      writeOut( out, "No class" );
    }
  }

  //TODO:TEMP!
  @Exposed( accessLevel = AccessLevel.ADMIN, outputType = MimeTypes.JSON )
  public void listTable( OutputStream out ) throws IOException, JSONException {
    String classTable = getRequestParameters().getStringParameter( "class", null );

    if ( classTable != null ) {
      writeOut( out, PushWarningsHandler.listClass( classTable ) );
    } else {
      writeOut( out, Result.getError( "No class" ) );
    }
  }

  //TODO:TEMP!
  @Exposed( accessLevel = AccessLevel.ADMIN, outputType = MimeTypes.JSON )
  public void listCda( OutputStream out ) throws IOException, JSONException {
    String dataAccessId = getRequestParameters().getStringParameter( "dataAccessId", null );
    String settingsId = getRequestParameters().getStringParameter( "cdaSettingsId", null );

    if ( !StringUtils.isEmpty( dataAccessId ) && !StringUtils.isEmpty( settingsId ) ) {
      writeOut( out, PushWarningsHandler.listClass( settingsId, dataAccessId ) );
    } else {
      writeOut( out, Result.getError( "Something missing" ) );
    }
  }

  //    //TODO:TEMP!
  //    @Exposed(outputType = MimeType.JSON)
  //    public void testCdaStuff(OutputStream out) throws Exception {
  //      JSONObject event = new JSONObject(
  //"{\"plugin\":\"cda\",\"eventType\":\"QueryTooLong\",\"timestamp\":\"1340291929309\",\"event\":{\"duration\":42,
  // \"queryInfo\":{\"dataAccessId\":\"olapQuery\",\"query\":\"some query\",\"cdaSettingsId\":\"testing/testeMdxCat
  // .cda\",\"parameters\":{}}}}");
  //      PushWarningsHandler h = new PushWarningsHandler();
  //      writeOut(out, h.call(event).toString(2));
  //    }
  private void callCDE( String file, OutputStream out ) throws UnsupportedEncodingException, IOException {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put( "solution", "system" );
    params.put( "path", "cdv/presentation/" );
    params.put( "file", file );
    params.put( "absolute", "false" );
    params.put( "inferScheme", "false" );

    IParameterProvider requestParams = getRequestParameters();
    PluginUtils.copyParametersFromProvider( params, WrapperUtils.wrapParamProvider( requestParams ) );

    if ( requestParams.hasParameter( "mode" ) && requestParams.getStringParameter( "mode", "Render" )
      .equals( "edit" ) ) {
      redirectToCdeEditor( out, params );
      return;
    }

    InterPluginCall pluginCall = new InterPluginCall( InterPluginCall.CDE, "Render", params );
    pluginCall.setResponse( getResponse() );
    pluginCall.setOutputStream( out );
    pluginCall.run();
  }

  private void redirectToCdeEditor( OutputStream out, Map<String, Object> params ) throws IOException {

    StringBuilder urlBuilder = new StringBuilder();
    urlBuilder.append( "../pentaho-cdf-dd/edit" );
    if ( params.size() > 0 ) {
      urlBuilder.append( "?" );
    }

    List<String> paramArray = new ArrayList<String>();
    for ( String key : params.keySet() ) {
      Object value = params.get( key );
      if ( value instanceof String ) {
        paramArray.add( key + "=" + URLEncoder.encode( (String) value, getEncoding() ) );
      }
    }

    urlBuilder.append( StringUtils.join( paramArray, "&" ) );
    redirect( urlBuilder.toString() );
  }

  @Override
  public RestRequestHandler getRequestHandler() {
    return Router.getBaseRouter();
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC, outputType = MimeTypes.JSON )
  public void about( OutputStream out ) throws IOException, JSONException {
    renderInCde( out, getRenderRequestParameters( "cdvAbout.wcdf" ) );

  }


  private Map<String, Object> getRenderRequestParameters( String dashboardName ) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put( "solution", "system" );
    params.put( "path", UI_PATH );
    params.put( "file", dashboardName );
    params.put( "bypassCache", "true" );
    params.put( "absolute", "true" );
    params.put( "inferScheme", "false" );
    params.put( "root", getRoot() );

    //add request parameters
    ServletRequest request = getRequest();
    @SuppressWarnings( "unchecked" )//should always be String
      Enumeration<String> originalParams = request.getParameterNames();
    // Iterate and put the values there
    while ( originalParams.hasMoreElements() ) {
      String originalParam = originalParams.nextElement();
      params.put( originalParam, request.getParameter( originalParam ) );
    }

    return params;
  }

  private void renderInCde( OutputStream out, Map<String, Object> params ) throws IOException {
    InterPluginCall pluginCall = new InterPluginCall( InterPluginCall.CDE, "Render", params );
    pluginCall.setResponse( getResponse() );
    pluginCall.setOutputStream( out );
    pluginCall.run();
  }

  private String getRoot() {

    ServletRequest wrapper = getRequest();
    String root = wrapper.getScheme() + "://" + wrapper.getServerName() + ":" + wrapper.getServerPort();

    return root;
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void checkVersion( OutputStream out ) throws IOException, JSONException {
    writeOut( out, getVersionChecker().checkVersion() );
  }

  /*@Exposed( accessLevel = AccessLevel.PUBLIC )
  public void refreshNotifications( OutputStream out ) throws IOException, JSONException {
    writeOut( out, getVersionChecker().checkVersion() );
  }  */

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void getVersion( OutputStream out ) throws IOException, JSONException {
    writeOut( out, getVersionChecker().getVersion() );
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

}
