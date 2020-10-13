package com.mykovol.takeandcharge.dataobj;

import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;

public class ErrorResponse implements PropertyBusinessObject {
    public final Property<String, UserCreationRequest> status = new Property<>("status");
    public final Property<String, UserCreationRequest> error = new Property<>("error");
    public final Property<String, UserCreationRequest> message = new Property<>("message");
    public final Property<String, UserCreationRequest> exception = new Property<>("exception");

    private final PropertyIndex idx = new PropertyIndex(this, "ErrorResponse", status, error, message, exception);

    @Override
    public PropertyIndex getPropertyIndex() {
        return idx;
    }
}
