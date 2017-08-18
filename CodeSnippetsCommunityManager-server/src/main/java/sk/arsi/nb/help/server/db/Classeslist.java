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
@Table(name = "CLASSESLIST")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Classeslist.findAll", query = "SELECT c FROM Classeslist c"),
    @NamedQuery(name = "Classeslist.findByClassname", query = "SELECT c FROM Classeslist c WHERE c.classname = :classname")})
public class Classeslist implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "CLASSNAME")
    private String classname;

    @ManyToMany(mappedBy = "classeslistList", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private List<Helps> helpsList = new ArrayList<>();

    public Classeslist() {
    }

    public Classeslist(String classname) {
        this.classname = classname;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
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
        hash += (classname != null ? classname.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Classeslist)) {
            return false;
        }
        Classeslist other = (Classeslist) object;
        if ((this.classname == null && other.classname != null) || (this.classname != null && !this.classname.equals(other.classname))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.apli.kafka.kafka.test.Classeslist[ classname=" + classname + " ]";
    }

}
