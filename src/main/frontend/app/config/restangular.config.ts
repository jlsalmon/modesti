export class RestangularConfig {

  public static configure(RestangularProvider:any) {
    // Set the base URL
    RestangularProvider.setBaseUrl('/api');

    // Enable access to the response headers
    RestangularProvider.setFullResponse(true);

    // Add a response interceptor
    RestangularProvider.addResponseInterceptor(function(data:any, operation:any) {

      var extractedData:any[];

      if (operation === "getList") {
        if (data.hasOwnProperty('_embedded')) {
          extractedData = data; //._embedded.requests;
        } else {
          extractedData = data.data;
        }
      } else {
        extractedData = data;
      }
      return extractedData;
    });

    // Set the self link
    RestangularProvider.setRestangularFields({
      selfLink : "_links.self.href"
    });
  }
}
