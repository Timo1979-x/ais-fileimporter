/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.xml;

import java.util.List;

/**
 *
 * @author ltv
 */
public interface IExchangeFile {
    public int getVersion();
    public String getDateRange();
    public String getDateGenerated();
    public List getDocs();
}
