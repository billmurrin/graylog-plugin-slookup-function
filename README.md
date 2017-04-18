[![Github Downloads](https://img.shields.io/github/downloads/billmurrin/graylog-plugin-slookup-function/total.svg)](https://github.com/billmurrin/graylog-plugin-slookup-function/releases)
[![GitHub Release](https://img.shields.io/github/release/billmurrin/graylog-plugin-slookup-function.svg)](https://github.com/billmurrin/graylog-plugin-slookup-function/releases)
[![Build Status](https://travis-ci.org/billmurrin/graylog-plugin-slookup-function.svg?branch=master)](https://travis-ci.org/billmurrin/graylog-plugin-slookup-function)

The GrayLog Stream Lookup (SLookup) Pipeline Processor function
---

SLookup facilitates lookups within other streams using a field in the current stream as the source value to match against a destination field in a remote stream. Where the query is sucessful, it will return the desired field from the remote stream.
       
For example, say there are two streams, one contains some http logs with source IPs (E.g. src_ip) from internal hosts and the other stream contains information about the systems on the network such as IP address (E.g. ip_address), computer name (E.g. computer_name), MAC address (E.g. mac_address), OU, make/model, etc. 
       
In the example above, you might want to return the mac_address field where the value of src_ip matches ip_address.

The thought behind this function is to implement similar functionality to the **VLOOKUP** function in Excel.

With features like index sets being introduced in Graylog 2.2, it becomes possible to use GrayLog to store smaller information stores that can be used to enrich other stream data with Pipeline Processor rules.

**Compiled and tested with Graylog 2.2.1, 2.2.2 and 2.2.3**

Function Breakdown
---

Function | Description
-------- | -----------
slookup(stream, srcField, dstField, rtnField, timeRange, sortOrder) : String | Conduct a lookup in a remote stream and return a field value based on a matching source field. Similar to VLOOKUP in Excel

Parameter | Type | Required | Description
--------- | ---- | -------- | -----------
stream  | String |  Y | The stream to look up the source field.
srcField | String | Y | The source field. The value to query for in the remote stream.
dstField | String | Y | The destination field that will be queried against.
rtnField | String |  Y | The field to return if the query is successful.
timeRange | String |  Y | Relative Time Range (Seconds)
sortOrder | String |  Y | Timestamp sort order either "asc" or "desc".

Use Case and Rule Example
---

Below are example rules that were created to lookup the value of an IP address in order enrich Windows Event Log messages coming from WinLogBeat.

In this use case, the remote stream named Systems with stream_id 58aba0cb3cbe8205e76c6145 contains system information (IP, MAC, ComputerName). This could be a dump of Directory Service Computer Objects, a listing of NBTScan results, etc. 

The slookup function constructs a search query using the value of winlogbeat_computer_name on the computer_name field (computer_name:VALUE_OF_FIELD). If the search is successful, the ip_address field is returned. The returned value can then be added to the current stream message in the pipeline.

The sortOrder parameter instructs the function to either return the oldest match (ascending), or the newest match (descending) if multiple records are found during the query.

```
rule "IP Lookup - Ascending"
when
    has_field("winlogbeat_computer_name")
then
    //StreamID, Source Field, Destination Field, Return Field, Relative Time, Ascending SortOrder
    let system_info = slookup("58aba0cb3cbe8205e76c6145", "winlogbeat_computer_name", "computer_name", "ip_address", "14400", "asc");
    set_field("ip_address", to_ip(system_info));
end
```

```
rule "IP Lookup - Descending"
when
    has_field("winlogbeat_computer_name")
then
    //StreamID, Source Field, Destination Field, Return Field, Relative Time, Descending SortOrder
    let system_info = slookup("58aba0cb3cbe8205e76c6145", "winlogbeat_computer_name", "computer_name", "ip_address", "14400", "desc");
    set_field("ip_address", to_ip(system_info));
end
```

Additional Info
---
This function has been tested in a limited setting. Its performance impact on very large remote streams and very large relative data timeframes, remains unknown.

If you experience an ingestion slow-down enriching a large volume of data, you can attempt increasing *processbuffer_processors* in the graylog server.conf file.

More information about writing a Graylog2 processor pipeline function.
https://www.graylog.org/blog/71-writing-your-own-graylog-processing-pipeline-functions
