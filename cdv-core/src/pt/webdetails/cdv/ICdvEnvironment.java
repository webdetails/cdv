/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2002 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package pt.webdetails.cdv;

import pt.webdetails.cpf.bean.IBeanFactory;
import pt.webdetails.cpf.exceptions.InitializationException;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;

public interface ICdvEnvironment {

  public void init( IBeanFactory factory ) throws InitializationException;

  public void refresh();

  public IContentAccessFactory getContentAccessFactory();

  public String getPluginRepositoryDir();

  public String getSystemDir();

  public String getPluginId();

  public String getJsDir();

  public String getTestsDir();

  public void ensureDefaultDirAndFilesExists();
}
