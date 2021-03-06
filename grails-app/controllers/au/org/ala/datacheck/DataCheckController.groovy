package au.org.ala.datacheck
import au.com.bytecode.opencsv.CSVReader
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.GetMethod

class DataCheckController {

  def biocacheService
  def darwinCoreService
  def authService
  def tagService

  static allowedMethods = [processData: "POST"]

  def noOfRowsToDisplay = 5

  def index = {
    [userId:authService.getUserId()]
  }

  def parseColumns = {

    log.debug("Content type>>" + request.getContentType())
    request.getHeaderNames().each { x -> log.debug(x + ": " + request.getHeader(x))}

    //is it comma separated or tab separated
    def raw = request.getReader().readLines().join("\n").trim()

    //def raw = request.getParameter("rawData").trim()
    log.debug("Unparsed RAW>> "  + raw)

    CSVReader csvReader = getCSVReaderForText(raw)

    //determine column headers
    def columnHeadersUnparsed = csvReader.readNext()

    log.debug("Unparsed>> "  + columnHeadersUnparsed)

    def columnHeaders = null
    def columnHeaderMap = null
    def firstLineIsData  = false
    def dataRows = new ArrayList<String[]>()

    //is the first line a set of column headers ??
    if(biocacheService.areColumnHeaders(columnHeadersUnparsed)){
      log.debug("First line of data recognised as darwin core terms")
      firstLineIsData = false
      columnHeaderMap = biocacheService.mapColumnHeaders(columnHeadersUnparsed)
    } else {
      //first line is data
      log.debug("First line of data is assumed to be data")
      firstLineIsData = true
      dataRows.add(columnHeadersUnparsed)
      columnHeaders = biocacheService.guessColumnHeaders(columnHeadersUnparsed)
    }

    log.debug("Parsed>> "  + columnHeaders + ", size: " + columnHeaders)

    def startAt = firstLineIsData ? 0 : 1

    def currentLine = csvReader.readNext()
    for(int i=startAt; i<noOfRowsToDisplay && currentLine!=null; i++){
      dataRows.add(currentLine)
      currentLine = csvReader.readNext()
    }
    // pass back HTML table
    if(firstLineIsData){
      render(view:'parsedData',  model:[columnHeaders:columnHeaders, dataRows:dataRows, firstLineIsData:firstLineIsData])
    } else {
      render(view:'parsedData',  model:[columnHeaderMap:columnHeaderMap, dataRows:dataRows, firstLineIsData:firstLineIsData])
    }
  }

  def parseColumnsWithFirstLineInfo = {

    //is it comma separated or tab separated
    def raw = request.getParameter("rawData").trim()
    def firstLineIsData = Boolean.parseBoolean(request.getParameter("firstLineIsData").trim())
    CSVReader csvReader = getCSVReaderForText(raw)

    //determine column headers
    def columnHeadersUnparsed = csvReader.readNext()

    log.debug("Unparsed>> "  + columnHeadersUnparsed)

    def columnHeaders = null
    def columnHeaderMap = null
    def dataRows = new ArrayList<String[]>()

    //is the first line a set of column headers ??
    if(firstLineIsData){
      log.debug("First line of data is assumed to be data")
      dataRows.add(columnHeadersUnparsed)
      columnHeaders = biocacheService.guessColumnHeaders(columnHeadersUnparsed)
    } else {
      //first line is data
      log.debug("First line of data recognised as darwin core terms")
      columnHeaderMap = biocacheService.mapColumnHeaders(columnHeadersUnparsed)
    }

    log.debug("Parsed>> "  + columnHeaders)
    def startAt = firstLineIsData ? 0 : 1

    def currentLine = csvReader.readNext()
    for(int i=startAt; i<noOfRowsToDisplay && currentLine!=null; i++){
      dataRows.add(currentLine)
      currentLine = csvReader.readNext()
    }
    // pass back HTML table
    if(firstLineIsData){
      render(view:'parsedData',  model:[columnHeaders:columnHeaders, dataRows:dataRows, firstLineIsData:firstLineIsData])
    } else {
      render(view:'parsedData',  model:[columnHeaderMap:columnHeaderMap, dataRows:dataRows, firstLineIsData:firstLineIsData])
    }
  }

  def getCSVReaderForText(String raw) {
    def separator = getSeparator(raw)
    def csvReader = new CSVReader(new StringReader(raw), separator.charAt(0))
    csvReader
  }

  def getSeparator(String raw) {
    int tabs = raw.count("\t")
    int commas = raw.count(",")
    if(tabs > commas)
      return '\t'
    else
      return ','
  }

  def getSeparatorName(String raw) {
    int tabs = raw.count("\t")
    int commas = raw.count(",")
    if(tabs > commas)
      return "TAB"
    else
      return "COMMA"
  }

  def processData = {

    def headers = null
    if(params.headers){
        headers = params.headers.split(",")
    }

    def csvData = params.rawData.trim()
    def firstLineIsData = Boolean.parseBoolean(params.firstLineIsData)

    //the data to pass back
    List<ParsedRecord> processedRecords = new ArrayList<ParsedRecord>()

    def counter = 0
    def csvReader = getCSVReaderForText(csvData)
    def currentLine = csvReader.readNext()
    if(firstLineIsData){
      counter += 1
      processedRecords.add(biocacheService.processRecord(headers, currentLine))
    }

    currentLine = csvReader.readNext()

    while(currentLine != null && counter <noOfRowsToDisplay){
      counter += 1
      processedRecords.add(biocacheService.processRecord(headers, currentLine))
      currentLine = csvReader.readNext()
    }

    render(view:'processedData',  model:[processedRecords:processedRecords])
  }

  def upload = {

    def userId = authService.getUserId()
    if(!userId){
      response.sendError(401)
      return null
    }

    //read the csv
    String headers = request.getParameter("headers").trim();
    String csvData = request.getParameter("rawData").trim()
    String separator = getSeparatorName(csvData)
    String datasetName = request.getParameter("datasetName").trim()
    String customIndexedFields = request.getParameter("customIndexedFields").trim();
    String firstLineIsData = request.getParameter("firstLineIsData")
    def responseString = biocacheService.uploadData(csvData, headers, datasetName, separator, firstLineIsData, customIndexedFields)
    response.setContentType("application/json")
    render(responseString)
  }

  def redirectToBiocache = {
    def http = new HttpClient()
    //reference the UID caches
    def get = new GetMethod(grailsApplication.config.sandboxHubsWebapp + "/occurrences/refreshUidCache")
    http.executeMethod(get)
    redirect(url:grailsApplication.config.sandboxHubsWebapp + "/occurrences/search?q=data_resource_uid:" + params.uid)
  }

  def redirectToSpatialPortal = {
    def http = new HttpClient()
    //reference the UID caches
    def get = new GetMethod(grailsApplication.config.sandboxHubsWebapp + "/occurrences/refreshUidCache")
    http.executeMethod(get)
    redirect(url:grailsApplication.config.spatialPortalUrl + "?q=data_resource_uid:" + params.uid + grailsApplication.config.spatialPortalUrlOptions)
  }

  def redirectToDownload = {
    def http = new HttpClient()
    //reference the UID caches
    def get = new GetMethod(grailsApplication.config.sandboxHubsWebapp + "/occurrences/refreshUidCache")
    http.executeMethod(get)
    //redirect(url:grailsApplication.config.biocacheServiceUrl + "/occurrences/index/download?q=data_resource_uid:" + params.uid + grailsApplication.config.biocacheServiceDownloadParams)
    redirect(url:grailsApplication.config.biocacheServiceUrl + "/occurrences/index/download?reasonTypeId=" + grailsApplication.config.downloadReasonId + "&q=data_resource_uid:" + params.uid + "&" + grailsApplication.config.biocacheServiceDownloadParams)
  }


  def uploadStatus = {
    log.debug("Request to retrieve upload status")
    def responseString
    if (params.tag) {
        params.uid = tagService.get(params.tag).uid
    }
    if (params.uid) {
        responseString = biocacheService.uploadStatus(params.uid)
        response.setContentType("application/json")
        if (params.tag) {
            //include uid in JSON response
            responseString = responseString.substring(0, responseString.length() - 1) + ",\"uid\":\"" + tagService.get(params.tag).uid + "\"}"
        }
        render(responseString)
    } else response.sendError(404)
    
  }

  def autocomplete = {
    def query = params.q
    //def limit = params.limit !=null ? params.limit.asType(Integer.class) : 10
    def list = darwinCoreService.autoComplete(query, 10)
    render(contentType:"application/json") {list}
  }
}