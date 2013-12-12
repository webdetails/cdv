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

  public static IUserContentAccess getUserContentAccess(){
    return getContentAccessFactory().getUserContentAccess("/");
  }

  public static IReadAccess getPluginRepositoryReader(){
    return getContentAccessFactory().getPluginRepositoryReader(null);
  }

  public static IReadAccess getPluginRepositoryReader(String initialPath){
    return getContentAccessFactory().getPluginRepositoryReader(initialPath);
  }

  public static IRWAccess getPluginRepositoryWriter(){
    return getContentAccessFactory().getPluginRepositoryWriter(null);
  }

  public static IRWAccess getPluginRepositoryWriter(String initialPath){
    return getContentAccessFactory().getPluginRepositoryWriter(initialPath);
  }

  public static IReadAccess getPluginSystemReader(){
    return getContentAccessFactory().getPluginSystemReader(null);
  }

  public static IReadAccess getPluginSystemReader(String initialPath){
    return getContentAccessFactory().getPluginSystemReader(initialPath);
  }

  public static IRWAccess getPluginSystemWriter(){
    return getContentAccessFactory().getPluginSystemWriter(null);
  }

  public static IReadAccess getOtherPluginSystemReader(String pluginId){
    return getContentAccessFactory().getOtherPluginSystemReader(pluginId, null);
  }

  public static IReadAccess getOtherPluginSystemReader(String pluginId, String initialPath){
    return getContentAccessFactory().getOtherPluginSystemReader(pluginId, initialPath);
  }

  /*public static IPluginResourceLocationManager getPluginResourceLocationManager(){
    return CbeEngine.getInstance().getEnvironment().getPluginResourceLocationManager();
  }

  public static IDataSourceManager getDataSourceManager(){
    return CdeEngine.getInstance().getEnvironment().getDataSourceManager();
  } */

  public static String getPluginRepositoryDir(){
    return CdvEngine.getInstance().getEnvironment().getPluginRepositoryDir();
  }

  public static String getPluginId(){
    return CdvEngine.getInstance().getEnvironment().getPluginId();
  }

  public static String getSystemDir(){
    return CdvEngine.getInstance().getEnvironment().getSystemDir();
  }

  /*public static IFileHandler getFileHandler(){
    return CdeEngine.getInstance().getEnvironment().getFileHandler();
  } */
}
