# Présentation

TiPi - Transactional Processing is an embedded library that provides Transactional Processing out of the box
Writing process is done entirely in Java, no XML, etc...
Transaction is also builtin so a process is either passed ans state is updated to OK or failed, operations are rollback and state is updated to ERROR
In error processes ca be retried at will.

# Development of process

## Integrating TiPi in your project

pom.xml
```
<dependency>
    <groupId>ch.sharedvd.tipi</groupId>
    <artifactId>tipi-starter</artifactId>
    <version>1.0.5</version>
</dependency>
```

## Writing process class 

```
@TipiTopProcess(description = "exploitation.deployer deployment")
public class ExplDeplDeployTopProcess extends TopProcess {

    @Override
    protected ActivityResultContext execute() throws Exception {
        ... some actions
    
        return new FinishedActivityResultContext();
    }
}
```

## Running process

```
    @Autowired
    private TipiFacade tipiFacade;

   ...
   public void myMethod() {
        ...
        long processId = tipiFacade.launch(ExplDeplDeployTopProcess.class, new VariableMap());
        ...
           
    }

```

# Reporting

You can query the state of the processes with TipiQueryFacade

## All processes
```
TipiCriteria criteria = new TipiCriteria(); // empty criteria -> all
TipiQueryFacade.searchActivities(criteria, -1)
```

## Specific processes
```
TipiCriteria criteria = new TipiCriteria();
criteria.setNameOrProcessName("ExplDeplDeployTopProcess");
TipiQueryFacade.searchActivities(criteria, -1)
```

## Specific processes with limit
```
TipiCriteria criteria = new TipiCriteria();
criteria.setNameOrProcessName("ExplDeplDeployTopProcess");
TipiQueryFacade.searchActivities(criteria, 10)
```
Returns only the 10 first found processes

# Releasing to Maven central

```
mvn release:prepare
mvn release:perform
```

Then to see errors:

https://oss.sonatype.org/

Login with Sonatype JIRA account
