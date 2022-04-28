# Mini-Framework

- Création d'un mini-framework java avec servlet

## Project Structure

    - models
    - service
    - controller
    - frontcontroller

## Models

    - We use the base.jar library found in https://github.com/Lacoste27/Base

```java
     public Administrateur() {
         this.setDatabaseName("contenu");
     }

     public Administrateur(Object id, Object personne_id, Object username, Object password) {
         this.setDatabaseName("contenu");
         this.id = String.valueOf(id);
         this.personne_id = String.valueOf(personne_id);
         this.username = String.valueOf(username);
         this.password = String.valueOf(password);
     }

     public Administrateur(Object personne_id, Object username, Object password) {
         this.setDatabaseName("contenu");
         this.personne_id = String.valueOf(personne_id);
         this.username = String.valueOf(username);
         this.password = String.valueOf(password);
     }
```

## Services

```java
    public class AdministrateurService {
    private Administrateur administrateur = new Administrateur();

    public AdministrateurService() {

    }

    public void setAdministrateur(Administrateur administrateur) {
        this.administrateur = administrateur ;
    }

    public int login() {
        return this.administrateur.login() ;
    }
}
```

## Controller

    - All class controller is annoted with  @ClassAnnotation(className = "the model")
    - All method in the controller is annoted with @MethodAnnotation.urlMethod(className = "the model", method = "the method name", url ="the url")
    - The controller names must be the model name + "Controller

```java
 private final AdministrateurService administrateurService = new AdministrateurService();
 private Administrateur administrateur = new Administrateur();
 private String page = "";

 public AdministrateurController() {

 }

 public void setAdministrateur(Administrateur administrateur) {
     this.administrateur = administrateur;
 }

 public void setPage(String page) {
     this.page = page ;
 }

 @MethodAnnotation.urlMethod(className = "Administrateur", method = "Login", url = "LoginPage.do")
 public ModelView Login() {
     ModelView retour = new ModelView();
     HashMap<String, List> hash = new HashMap<>();
     this.setPage("Login");
     retour.setHash(hash);
     retour.setPage(this.page);
     return retour;
 }
```

## FrontController

    - For downloading a file you , you need to create the basecontroller class which is a daughter class of frontcontroller and the class is annotated by @MultipartConfig(filesizethreshold = 1024*1024, maxfilesize = 1024*1024*5, maxrequestsize = 1024*1024*5*5)

```java
@MultipartConfig(fileSizeThreshold = 1024*1024, maxFileSize = 1024*1024*5, maxRequestSize = 1024*1024*5*5)
public class BaseController extends FrontController {

}
```

## Web.xml
`<?xml version="1.0" encoding="UTF-8"?>
<web-app version="3.1" xmlns="http://xmlns.jcp.org/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd">
    <servlet>
        <servlet-name>Framework</servlet-name>
        <servlet-class>frontcontroller.BaseController</servlet-class>
    </servlet>
    <servlet>
        <servlet-name>Download</servlet-name>
        <servlet-class>controller.FileController</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>Framework</servlet-name>
        <url-pattern>*.do</url-pattern>
    </servlet-mapping>
    <servlet-mapping>
        <servlet-name>Download</servlet-name>
        <url-pattern>/download/fichier</url-pattern>
    </servlet-mapping>
    <session-config>
        <session-timeout>
            30
        </session-timeout>
    </session-config>
</web-app>
`
