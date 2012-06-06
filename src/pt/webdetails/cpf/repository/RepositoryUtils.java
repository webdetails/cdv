/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cpf.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 *
 * @author pdpi
 */
public class RepositoryUtils {

  public final static String ENCODING = "utf-8";
  private static final Log logger = LogFactory.getLog(RepositoryUtils.class);

  public static void writeSolutionFile(String path, String fileName, String data) {
    try {
      ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, PentahoSessionHolder.getSession());
      solutionRepository.publish(PentahoSystem.getApplicationContext().getSolutionPath(""), path, fileName, data.getBytes(ENCODING), true);
    } catch (Exception e) {
      logger.error(e);
    }
  }

  public static void deleteSolutionFile(String path, String fileName) {
    try {
      String fullPath = path + "/" + fileName.replaceAll("\\\\", "/").replaceAll("/+", "/");

      ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, PentahoSessionHolder.getSession());
      solutionRepository.removeSolutionFile(fullPath);
    } catch (Exception e) {
      logger.error(e);
    }
  }

  public static void moveSolutionFile(String path, String fileName, String newPath, String newFileName) {
    /* Seems like there's no actual 'rename' functionality in the
     * solution repo, so we're stuck reading in the whole file,
     * writing it as a new file, then finally deleting the old one.
     */
    copySolutionFile(path, fileName, newPath, newFileName, true);
  }

  public static void copySolutionFile(String path, String fileName, String newPath, String newFileName) {
    copySolutionFile(path, fileName, newPath, newFileName, false);
  }

  private static void copySolutionFile(String path, String fileName, String newPath, String newFileName, boolean deleteOld) {
    try {

      String fullPath = path + "/" + fileName.replaceAll("\\\\", "/").replaceAll("/+", "/");
      ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, PentahoSessionHolder.getSession());
      byte[] data = solutionRepository.getResourceAsBytes(fullPath, true, 0);
      solutionRepository.publish(PentahoSystem.getApplicationContext().getSolutionPath(""), newPath, newFileName, data, true);
      if (deleteOld && !(newPath.equals(path) && newFileName.equals(fileName) )) {
        solutionRepository.removeSolutionFile(fullPath);
      }
    } catch (Exception e) {
      logger.error(e);
    }
  }

}
