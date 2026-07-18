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


package pt.webdetails.cdv.bean.factory;

public interface ICdvBeanFactory {

  public Object getBean( String id );

  public boolean containsBean( String id );

  public String[] getBeanNamesForType( @SuppressWarnings("rawtypes") Class clazz );
}