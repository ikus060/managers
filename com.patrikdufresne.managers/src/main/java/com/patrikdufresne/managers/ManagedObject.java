/**
 * Copyright(C) 2013 Patrik Dufresne Service Logiciel <info@patrikdufresne.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.patrikdufresne.managers;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * This class is a based to other database object.
 */
@MappedSuperclass
public class ManagedObject implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2752896880322357422L;

    protected PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /**
     * Define the creation date of this object.
     * 
     * @uml.property name="creationDate"
     */
    private Date creationDate;

    /**
     * Unique identifier auto generated by hibernate.
     * 
     * @uml.property name="id"
     */
    protected Integer id;

    private Date modificationDate;

    /**
     * Java bean function.
     * 
     * @param listener
     *            The PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Java bean function.
     * 
     * @param propertyName
     *            The name of the property to listen on.
     * @param listener
     *            The PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        this.changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ManagedObject other = (ManagedObject) obj;
        if (this.id == null || other.id == null) {
            return false;
        } else if (!this.id.equals(other.id)) return false;
        return true;
    }

    /**
     * Getter of the property <tt>creationDate</tt>
     * 
     * @return Returns the creationDate.
     * @uml.property name="creationDate"
     */
    @Column(nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getCreationDate() {
        return this.creationDate;
    }

    /**
     * Getter of the property <tt>id</tt>
     * 
     * @return Returns the id.
     * @uml.property name="id"
     */
    @Id()
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer getId() {
        return this.id;
    }

    /**
     * Getter of the property <tt>modificationDate</tt>
     * 
     * @return Returns the modificationDate.
     * @uml.property name="modificationDate"
     */
    @Column(nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getModificationDate() {
        return this.modificationDate;
    }

    @Override
    public int hashCode() {
        return (this.id == null) ? 0 : this.id.hashCode();
    }

    /**
     * Java bean function.
     * 
     * @param propertyName
     * @return
     */
    public boolean hasListeners(String propertyName) {
        return this.changeSupport.hasListeners(propertyName);
    }

    /**
     * Java bean function.
     * 
     * @param listener
     *            The PropertyChangeListener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.changeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Java bean function.
     * 
     * @param propertyName
     *            The name of the property that was listened on.
     * @param listener
     *            The PropertyChangeListener to be removed
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        this.changeSupport.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * Setter of the property <tt>creationDate</tt>
     * 
     * @param creationDate
     *            The creationDate to set.
     * @uml.property name="creationDate"
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Setter of the property <tt>id</tt>
     * 
     * @param id
     *            The id to set.
     * @uml.property name="id"
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Setter of the property <tt>modificationDate</tt>
     * 
     * @param modificationDate
     *            The modificationDate to set.
     * @uml.property name="modificationDate"
     */
    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    @Override
    public String toString() {
        return "ManagedObject [id=" + this.id + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
