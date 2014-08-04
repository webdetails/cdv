wd.cdv.validators.registerValidator("custom", function(validation, rs){
  var validationResult = wd.cdv.validationResult({
      name: validation.validationName, 
      type: validation.validationType
  });

  var result = validation.validationFunction.call(this,rs,[]);

  if (typeof result == "object") {
    validationResult.setAlert(wd.cdv.alert(result));
  } else {
    validationResult.setAlert(this.parseAlert(result));
    switch(result){
      case "OK":
        if(validation.successMessage) validationResult.getAlert().setDescription(validation.successMessage);
        break;
      case "CRITICAL":
      case "ERROR":
      case "WARNING":
      default:
        if(validation.failureMessage) validationResult.getAlert().setDescription(validation.failureMessage);
        break;
    }
  }
  return validationResult;
});
