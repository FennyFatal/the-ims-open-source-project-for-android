package javax.microedition.ims.core.sipservice.subscribe;

import javax.microedition.ims.common.EventPackage;

/**
 * Created by IntelliJ IDEA.
 * User: Labada
 * Date: 2/8/11
 * Time: 11:03 AM
 * To change this template use File | Settings | File Templates.
 */
class SubscriptionKeyDefaultImpl implements SubscriptionKey {

    private final String value;
    private final EventPackage event;

    SubscriptionKeyDefaultImpl(
            final String value,
            final EventPackage event) {

        this.value = value;
        this.event = event;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubscriptionKeyDefaultImpl that = (SubscriptionKeyDefaultImpl) o;

        if (event != that.event) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (event != null ? event.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SubscriptionKeyDefaultImpl{}";
    }
}
