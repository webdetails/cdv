lib("lib/underscore.js");
registerHandler("GET", "/hi", function(out){
  var datasource = datasourceFactory.createDatasource('cda');
  datasource.setDefinitionFile("/plugin-samples/cda/cdafiles/mondrian-jndi.cda");
  datasource.setDataAccessId('1');

  //datasource.setParameter('referenceDateParameter', paramreferenceDateParameter);
  this.setOutputType(this.MIME_JSON);
  out.write(datasource.execute().toString().getBytes("utf-8"));
});

registerHandler("GET", "/underscore", function(out){
  this.setOutputType(this.MIME_TEXT);
  _.each([1,2,3],function(e){
  out.write(new java.lang.String(e + "\n").getBytes("utf-8"));
    
  });
  out.write(new java.lang.String("Hello there mate!").getBytes("utf-8"));
  //out.write(datasource.execute().toString().getBytes("utf-8"));
  return "";
});

registerHandler("GET", "/foobar", function(out){
  this.setOutputType(this.MIME_TEXT);
  _.each([1,2,3],function(e){
  out.write(new java.lang.String(e * 2 + "\n").getBytes("utf-8"));
    
  });
  out.write(new java.lang.String("Hello there mate!").getBytes("utf-8"));
  //out.write(datasource.execute().toString().getBytes("utf-8"));
  return "";
});
