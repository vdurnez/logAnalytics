# Overview
This application extracts popular queries from logs:
* count *distinct queries* within a timeframe (cardinality)
  `GET /1/queries/count/2014-01-02`: returns `{ count: 12345 }` where dateFormat can be "yyyy", "yyyy-MM", "yyyy-MM-dd", "yyyy-MM-dd HH", yyyy-MM-dd HH:mm"
* get *top queries* within a timeframe (top-k streaming algo)
  `GET /1/queries/popular/2015?size=5` returns `{ queries: {query: "xxx", count:123}, ...}`


## Technical
Application is built with spring-boot & java 8

### How-to start
`mvn spring-boot:run -D logfile=fileName.tsv`

log file
* "logfile" parameter default value is "/tmp/logs.tsv" 
* log file line format is "timestamp <tab> item" where timestamp format is "yyyy-MM-dd HH:mm:ss"
* log file MUST have ordered timestamps (if not, do "sort -k1 file > fileSorted")

Then execute queries on localhost:8080
* http://localhost:8080/1/queries/count/2015-08-02
* http://localhost:8080/1/queries/popular/2015-08-02?size=2


### count distinct queries
Logic is based on simple HashSet (exact answer)

### Top queries
Logic is based on top-k streaming algorithm
capacity is set to 5.000 to have good approximate answer


## Limitations, improvements
Application reads 1 logs file at each query (declared by parameter "logfile")
logs input is time-indexed at startup (minute-level) to improve performance.

Count distinct queries provides an exact value but with a big memory footprint 
