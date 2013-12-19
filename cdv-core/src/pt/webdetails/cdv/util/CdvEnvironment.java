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

package pt.webdetails.cdv.util;

import pt.webdetails.cdv.CdvEngine;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;

public class CdvEnvironment {

  public static IContentAccessFactory getContentAccessFactory() {
    return CdvEngine.getInstance().getEnvironment().getContentAccessFactory();
  }

  public static IUserContentAccess getUserContentAccess() {
    return getContentAccessFactory().getUserContentAccess( "/" );
  }

  public static IReadAccess getPluginRepositoryReader() {
    return getContentAccessFactory().getPluginRepositoryReader( null );
  }

  public static IReadAccess getPluginRepositoryReader( String initialPath ) {
    return getContentAccessFactory().getPluginRepositoryReader( initialPath );
  }

  public static IRWAccess getPluginRepositoryWriter() {
    return getContentAccessFactory().getPluginRepositoryWriter( null );
  }

  public static IRWAccess getPluginRepositoryWriter( String initialPath ) {
    return getContentAccessFactory().getPluginRepositoryWriter( initialPath );
  }

  public static IReadAccess getPluginSystemReader() {
    return getContentAccessFactory().getPluginSystemReader( null );
  }

  public static IReadAccess getPluginSystemReader( String initialPath ) {
    return getContentAccessFactory().getPluginSystemReader( initialPath );
  }

  public static IRWAccess getPluginSystemWriter() {
    return getContentAccessFactory().getPluginSystemWriter( null );
  }

  public static IReadAccess getOtherPluginSystemReader( String pluginId ) {
    return getContentAccessFactory().getOtherPluginSystemReader( pluginId, null );
  }

  public static IReadAccess getOtherPluginSystemReader( String pluginId, String initialPath ) {
    return getContentAccessFactory().getOtherPluginSystemReader( pluginId, initialPath );
  }

  public static String getPluginRepositoryDir() {
    return CdvEngine.getInstance().getEnvironment().getPluginRepositoryDir();
  }

  public static String getPluginId() {
    return CdvEngine.getInstance().getEnvironment().getPluginId();
  }

  public static String getSystemDir() {
    return CdvEngine.getInstance().getEnvironment().getSystemDir();
  }

  public static String getJsDir() {
    return CdvEngine.getInstance().getEnvironment().getJsDir();
  }

  public static String getTestDir() {
    return CdvEngine.getInstance().getEnvironment().getTestsDir();
  }

  public static void ensureDefaultDirAndFilesExists()
  {
    CdvEngine.getInstance().getEnvironment().ensureDefaultDirAndFilesExists();
  }
}