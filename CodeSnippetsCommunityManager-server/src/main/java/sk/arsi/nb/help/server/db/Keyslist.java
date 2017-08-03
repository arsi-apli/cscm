/*
 * (C) Copyright 2017 Arsi (http://www.arsi.sk/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package sk.arsi.nb.help.server.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author arsi
 */
@Entity
@Table(name = "KEYSLIST")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Keyslist.findAll", query = "SELECT k FROM Keyslist k"),
    @NamedQuery(name = "Keyslist.findByKeyname", query = "SELECT k FROM Keyslist k WHERE k.keyname = :keyname")})
public class Keyslist implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "KEYNAME")
    private String keyname;
    @ManyToMany(mappedBy = "keyslistList", cascade = CascadeType.ALL)
    private List<Helps> helpsList = new ArrayList<>();

    public Keyslist() {
    }

    public Keyslist(String keyname) {
        this.keyname = keyname;
    }

    public String getKeyname() {
        return keyname;
    }

    public void setKeyname(String keyname) {
        this.keyname = keyname;
    }

    @XmlTransient
    public List<Helps> getHelpsList() {
        return helpsList;
    }

    public void setHelpsList(List<Helps> helpsList) {
        this.helpsList = helpsList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (keyname != null ? keyname.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Keyslist)) {
            return false;
        }
        Keyslist other = (Keyslist) object;
        if ((this.keyname == null && other.keyname != null) || (this.keyname != null && !this.keyname.equals(other.keyname))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.apli.kafka.kafka.test.Keyslist[ keyname=" + keyname + " ]";
    }

}
