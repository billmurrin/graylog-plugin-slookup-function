[![Github Downloads](https://img.shields.io/github/downloads/billmurrin/graylog-plugin-slookup-function/total.svg)](https://github.com/billmurrin/graylog-plugin-slookup-function/releases)
[![GitHub Release](https://img.shields.io/github/release/billmurrin/graylog-plugin-slookup-function.svg)](https://github.com/billmurrin/graylog-plugin-slookup-function/releases)
[![Build Status](https://travis-ci.org/billmurrin/graylog-plugin-slookup-function.svg?branch=master)](https://travis-ci.org/billmurrin/graylog-plugin-slookup-function)

The GrayLog Stream Lookup (SLookup) Pipeline Processor function
---

SLookup facilitates the lookup of a local stream's field value on a remote stream field, and if it matches, returns the requested fields for enrichment in the source stream.
       
For example, say there are two streams, one contains some http logs with source IPs (E.g. `src_ip`) from internal hosts and the other stream contains information about the systems on the network such as IP address (E.g. `ip_address`), computer name (E.g. `computer_name`), MAC address (E.g. `mac_address`), OU, make/model, etc. 
       
In the example above, you might want to return the `computer_name` and `mac_address` fields where the value of `src_ip` matches `ip_address`.

The thought behind this function is to implement a similar functionality to the **VLOOKUP** function in Excel.

With features like index sets being introduced in Graylog 2.x, it is possible to use data in one stream to enrich data in another with Pipeline Processor rules.

**Version 2.0.0 tested to work with Graylog 2.3.2 and 2.4.0**

Function Breakdown
---

Function | Description
-------- | -----------
slookup(stream, srcField, dstField, rtnField, timeRange, sortOrder) : List | Conduct a lookup in a remote stream and return a list of field(s) values based on a matching source field. Similar to VLOOKUP in Excel

Parameter | Type | Required | Description
--------- | ---- | -------- | -----------
stream  | String |  Y | The stream to look up the source field.
srcField | String | Y | The source field. The value to query for in the remote stream.
dstField | String | Y | The destination field that will be queried against.
rtnField | List |  Y | The field(s) to return if the query is successful.
timeRange | String |  Y | Relative Time Range (Seconds)
sortOrder | String |  Y | Timestamp sort order either "asc" or "desc".

Use Case and Rule Examples
---

Below are a couple example rules that help demonstrate how to use `slookup`.

In the following examples, the remote stream named Systems with stream_id `5a5d8854315d00059dbea98f` contains system information (IP, MAC, Operating System). The source of this data might be Directory Service Computer Objects, NBTScan results, Discovery Scan, etc. 

The `slookup` function constructs a search query using the value of `winlogbeat_computer_name` on the `computer_name` field (*computer_name:VALUE_OF_FIELD*). If the search is successful, the requested field(s) are returned. The list of values can then be added to the current stream message in the pipeline.

The `sortOrder` parameter instructs the function to either return the oldest match (ascending), or the newest match (descending) if multiple records are found during the query.

The Return Fields is a List object starting with index 0. The order of the indexes is based on the order you specified them in the return field List. If no search result is found for the field, it will return *"No match found"*.

#### Match on Computer Name, Return IP, Operating System and Mac Address, Use Newest (Descending Sort Order) result.
```
rule "Log Enrichment - Ascending"
when
    has_field("winlogbeat_computer_name")
then
    //StreamID, Source Field, Destination Field, Return Field(s), Relative Time, Ascending SortOrder
    let system_info = slookup("5a5d8854315d00059dbea98f", "winlogbeat_computer_name", "computer_name", ["ip_address","operating_system","mac_address"], "300", "desc");
    set_field("ip_address", system_info[0]);
    set_field("operating_system", to_string(system_info[1]));
    set_field("mac_address", system_info[2]);
end
```

#### Match on Computer Name, Return only IP, Use Oldest (Ascending Sort Order) result.
```
rule "IP Lookup - Descending"
when
    has_field("winlogbeat_computer_name")
then
    //StreamID, Source Field, Destination Field, Return Field, Relative Time, Descending SortOrder
    let system_info = slookup("5a5d8854315d00059dbea98f", "winlogbeat_computer_name", "computer_name", ["ip_address"], "14400", "asc");
    set_field("ip_address", to_ip(system_info));
end
```

Additional Info
---
This function's performance impact on very large remote streams and very large relative data timeframes, remains unknown.

If you experience an ingestion slow-down enriching a large volume of data, you can attempt increasing *processbuffer_processors* in the graylog server.conf file.

More information about writing a Graylog2 processor pipeline function.
https://www.graylog.org/blog/71-writing-your-own-graylog-processing-pipeline-functions
