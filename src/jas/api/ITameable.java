package jas.api;

/**
 * Must be implemented by Entities to be compatible with the IsTamed and IsTameable tags.
 * 
 * For Entities that extends EntityTameable do not need to implement this. IsTamed tag will consult the
 * EntityTameable.isTameable() method. IsTameable will always be true. If ITameable is implemented in conjunction with
 * extending EntityTameable, it will override the calls to the EntityTameable checks.
 */
public interface ITameable {
    public boolean isTamed();

    public boolean isTameable();
}
