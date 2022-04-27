/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelview;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Tsiory
 */
public class ModelView {
    private HashMap<String, List> liste ;
    private String page ;
    
    public ModelView() {
        
    }
    
    public HashMap<String, List> getHash() {
        return this.liste ;
    }
    
    public String getPage() {
        return this.page ;
    }
    
    public void setHash(HashMap<String , List> liste) {
        this.liste = liste ;
    }
    
    public void setPage(String page) {
        this.page = page ;
    }
}
