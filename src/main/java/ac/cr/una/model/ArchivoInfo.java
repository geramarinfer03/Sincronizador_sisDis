/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.cr.una.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 *
 * @author jose
 */
public class ArchivoInfo implements Serializable {

    private String fileName;
    private String MD5_File;
    private Date fecha_modificacion;
    private long file_size;
    private boolean is_dir;
    private int version;
    private boolean modified;
    private int action; //0 create/update 1 delete

    public ArchivoInfo() {
    }

    public ArchivoInfo(String fileName, Date fecha_modificacion, long file_size, boolean is_dir, int version, boolean modified) {
        this.fileName = fileName;
        this.fecha_modificacion = fecha_modificacion;
        this.file_size = file_size;
        this.is_dir = is_dir;
        this.version = version;
        this.modified = modified;
        this.action = 0;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.fileName);
        hash = 29 * hash + Objects.hashCode(this.MD5_File);
        hash = 29 * hash + Objects.hashCode(this.fecha_modificacion);
        hash = 29 * hash + (int) (this.file_size ^ (this.file_size >>> 32));
        hash = 29 * hash + (this.is_dir ? 1 : 0);
        hash = 29 * hash + this.version;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ArchivoInfo other = (ArchivoInfo) obj;

        if (!Objects.equals(this.fileName, other.fileName)) {
            return false;
        }
        if (!Objects.equals(this.MD5_File, other.MD5_File)) {
            return false;
        }
        /*if (!Objects.equals(this.version, other.version)) {
            return false;
        }*/
        return true;
    }

    public boolean copy(ArchivoInfo archivo) {
        this.setMD5_File(archivo.getMD5_File());
        this.setFileName(archivo.getFileName());
        this.setAction(archivo.getAction());
        this.setFile_size(archivo.getFile_size());
        this.setIs_dir(archivo.isIs_dir());
        this.setModified(archivo.isModified());
        this.setVersion(archivo.getVersion());
        this.setFecha_modificacion(archivo.getFecha_modificacion());
        return true;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMD5_File() {
        return MD5_File;
    }

    public void setMD5_File(String MD5_File) {
        this.MD5_File = MD5_File;
    }

    public Date getFecha_modificacion() {
        return fecha_modificacion;
    }

    public void setFecha_modificacion(Date fecha_modificacion) {
        this.fecha_modificacion = fecha_modificacion;
    }

    public long getFile_size() {
        return file_size;
    }

    public void setFile_size(long file_size) {
        this.file_size = file_size;
    }

    public boolean isIs_dir() {
        return is_dir;
    }

    public void setIs_dir(boolean is_dir) {
        this.is_dir = is_dir;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "ArchivoInfo{" + "fileName=" + fileName + ", MD5_File="
                + MD5_File + ", fecha_modificacion=" + fecha_modificacion
                + ", file_size=" + file_size + ", is_dir=" + is_dir
                + ", version=" + version + '}';
    }

}
