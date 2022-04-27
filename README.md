# Mini-Framework
* Création d'un mini-framework java avec servlet

## Project Structure 
    - models
    - service
    - controller
    - frontcontroller

## Models
    * We use the base.jar library found in https://github.com/Lacoste27/Base
   ```java
   - public Administrateur() {
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
        
