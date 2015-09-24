# Overview
This application extracts popular queries from massive logs
* distinct queries within a timeframe (cardinality)
  `GET /1/queries/count/2014-01-02`: returns `{ count: 12345 }` where dateFormat can be "yyyy", "yyyy-MM", "yyyy-MM-dd", "yyyy-MM-dd HH", yyyy-MM-dd HH:mm"
* top queries within a timeframe (top-k streaming algo
  `GET /1/queries/popular/2015?size=5` returns `{ queries: {query: "xxx", count:123}, ...}`


## Technical
Application is built with spring-boot & java 8

### How-to start
`mvn spring-boot:run -D logfile=fileName.tsv`
where fileName.tsv is log file (tabular), default value is "/tmp/logs.tsv" 


### Distinct queries
Logic is based on simple HashSet (exact answer)

### Top queries
Logic is based on top-k streaming algorithm
capacity is set to 5.000 to have good approximate answer


## Limitations, improvements
Application reads 1 logs file at each query (declared by parameter "logfile")

logs input could be time-indexed to improve performance
