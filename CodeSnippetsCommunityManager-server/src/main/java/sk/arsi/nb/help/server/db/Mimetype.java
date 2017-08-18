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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author arsi
 */
@Entity
@Table(name = "MIMETYPE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Mimetype.findAll", query = "SELECT m FROM Mimetype m"),
    @NamedQuery(name = "Mimetype.findByMimetype", query = "SELECT m FROM Mimetype m WHERE m.mimetype = :mimetype"),
    @NamedQuery(name = "Mimetype.findByDescription", query = "SELECT m FROM Mimetype m WHERE m.description = :description")})
public class Mimetype implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "MIMETYPE")
    private String mimetype;
    @Basic(optional = false)
    @Column(name = "DESCRIPTION")
    private String description;
    @OneToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, mappedBy = "mimetype")
    private List<Helps> helpsList = new ArrayList<>();

    public Mimetype() {
    }

    public Mimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        hash += (mimetype != null ? mimetype.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Mimetype)) {
            return false;
        }
        Mimetype other = (Mimetype) object;
        if ((this.mimetype == null && other.mimetype != null) || (this.mimetype != null && !this.mimetype.equals(other.mimetype))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sk.arsi.nb.help.server.Mimetype[ mimetype=" + mimetype + " ]";
    }

}
