package com.autonomy.abc.selenium.find.application;

public abstract class HodFind<T extends HodFindElementFactory> extends FindApplication<T> {

    private T elementFactory;

    @Override
    public T elementFactory() {
        return elementFactory;
    }

    public void setElementFactory(T elementFactory) {
        this.elementFactory = elementFactory;
    }

    @Override
    public boolean isHosted() {
        return true;
    }

    public static HodFind<? extends HodFindElementFactory> withRole(final UserRole role) {
        if(role == null){
            return new FindHodFind();
        }

        switch (role) {
            case BIFHI:
                return new BIHodFind();
            case FIND:
                return new FindHodFind();
            default:
                throw new IllegalStateException("Unsupported user role: " + role);
        }
    }
}
