/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author acer
 */
public class Result {
    
    String key;
    String link;
    
    Result(String key,String link)
    {
        this.key=key;
        this.link=link;
    }
    
    String getKey()
    {
        return key;
    }
    
    String getLink()
    {
        return link;
    }
}
