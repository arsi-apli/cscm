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
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author arsi
 */
@Entity
@Table(name = "RANKS")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Ranks.findAll", query = "SELECT r FROM Ranks r"),
    @NamedQuery(name = "Ranks.findByIdrank", query = "SELECT r FROM Ranks r WHERE r.idrank = :idrank"),
    @NamedQuery(name = "Ranks.findByRank", query = "SELECT r FROM Ranks r WHERE r.rank = :rank"),
    @NamedQuery(name = "Ranks.findByUser", query = "SELECT r FROM Ranks r WHERE r.user = :user")})
public class Ranks implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "IDRANK")
    private Integer idrank;
    @Column(name = "RANK")
    private Integer rank;
    @Column(name = "USERNAME")
    private String user;
    @JoinColumn(name = "helps_idhelps", referencedColumnName = "IDHELPS")
    @ManyToOne(optional = false, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private Helps helpsIdhelps;

    public Ranks() {
    }

    public Ranks(Integer idrank) {
        this.idrank = idrank;
    }

    public Integer getIdrank() {
        return idrank;
    }

    public void setIdrank(Integer idrank) {
        this.idrank = idrank;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Helps getHelpsIdhelps() {
        return helpsIdhelps;
    }

    public void setHelpsIdhelps(Helps helpsIdhelps) {
        this.helpsIdhelps = helpsIdhelps;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idrank != null ? idrank.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Ranks)) {
            return false;
        }
        Ranks other = (Ranks) object;
        if ((this.idrank == null && other.idrank != null) || (this.idrank != null && !this.idrank.equals(other.idrank))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sk.arsi.nb.help.server.Ranks[ idrank=" + idrank + " ]";
    }

}
