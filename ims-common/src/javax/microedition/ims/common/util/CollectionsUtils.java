/*
 * This software code is (c) 2010 T-Mobile USA, Inc. All Rights Reserved.
 *
 * Unauthorized redistribution or further use of this material is
 * prohibited without the express permission of T-Mobile USA, Inc. and
 * will be prosecuted to the fullest extent of the law.
 *
 * Removal or modification of these Terms and Conditions from the source
 * or binary code of this software is prohibited.  In the event that
 * redistribution of the source or binary code for this software is
 * approved by T-Mobile USA, Inc., these Terms and Conditions and the
 * above copyright notice must be reproduced in their entirety and in all
 * circumstances.
 *
 * No name or trademarks of T-Mobile USA, Inc., or of its parent company,
 * Deutsche Telekom AG or any Deutsche Telekom or T-Mobile entity, may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" AND "WITH ALL FAULTS" BASIS
 * AND WITHOUT WARRANTIES OF ANY KIND.  ALL EXPRESS OR IMPLIED
 * CONDITIONS, REPRESENTATIONS OR WARRANTIES, INCLUDING ANY IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT CONCERNING THIS SOFTWARE, ITS SOURCE OR BINARY CODE
 * OR ANY DERIVATIVES THEREOF ARE HEREBY EXCLUDED.  T-MOBILE USA, INC.
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE
 * OR ITS DERIVATIVES.  IN NO EVENT WILL T-MOBILE USA, INC. OR ITS
 * LICENSORS BE LIABLE FOR LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES,
 * HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT
 * OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF T-MOBILE USA,
 * INC. HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * THESE TERMS AND CONDITIONS APPLY SOLELY AND EXCLUSIVELY TO THE USE,
 * MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE, ITS SOURCE OR BINARY
 * CODE OR ANY DERIVATIVES THEREOF, AND ARE SEPARATE FROM ANY WRITTEN
 * WARRANTY THAT MAY BE PROVIDED WITH A DEVICE YOU PURCHASE FROM T-MOBILE
 * USA, INC., AND TO THE EXTENT PERMITTED BY LAW.
 */

package javax.microedition.ims.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides utility methods for Collection instances.
 *
 * @author ext-akhomush
 */
public final class CollectionsUtils {
    private CollectionsUtils() {
        assert false;
    }

    /**
     * Answers true if a predicate is true for at least one element of a
     * collection.
     *
     * @param <T>
     * @param collection - the collection to get the input from, may be null
     * @param value      - value to check
     * @return true if a collection contains specified value.
     */
    public static <T> boolean contains(Collection<T> collection, T value) {
        return collection != null && collection.size() > 0 && collection.contains(value);
    }

    /**
     * Answers true if a predicate is true for at least one element of a
     * collection.
     *
     * @param <T>        - type of elements in collection
     * @param collection - the collection to get the input from
     * @param predicate  - the predicate to use, may be null
     * @return true if at least one element of the collection matches the
     *         predicate
     * @throws java.lang.IllegalArgumentException
     *          - if the Collection or Predicate is null
     */
    public static <T> boolean exists(Collection<T> collection,
                                     Predicate<T> predicate) {
        return find(collection, predicate) != null;
    }

    /**
     * Find element by specified predicate.
     *
     * @param <T>        - type of elements in collection
     * @param collection - the collection to get the input from
     * @param predicate  - the predicate to use, may be null
     * @return element or null
     * @throws IllegalArgumentException - if the Collection or Predicate is null
     */
    public static <T> T find(Collection<T> collection, Predicate<T> predicate) {
        T retValue = null;
        if (collection == null || predicate == null) {
            throw new IllegalArgumentException();
        }

        for (T elem : collection) {
            if (predicate.evaluate(elem)) {
                retValue = elem;
                break;
            }
        }
        return retValue;
    }

    public static <T> void replaceOrAdd(T elementToAdd, Collection<T> collection, Predicate<T> predicate) {
        if (collection == null || elementToAdd == null || predicate == null) {
            throw new IllegalArgumentException();
        }

        T elemToReplace = find(collection, predicate);
        if (elemToReplace != null) {
            collection.remove(elemToReplace);
        }
        collection.add(elementToAdd);
    }

    /**
     * Null-safe check if the specified collection is empty.
     *
     * @param <T>
     * @param collection - the collection to check, may be null
     * @return true if empty or null
     */
    public static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.size() == 0;
    }

    /**
     * Returns a transformed bag backed by the given collection.
     * <p/>
     * Each object is passed through the transformer as it is added to the
     * Collection.
     *
     * @param <S>         - type for element in source collection
     * @param <D>         - type for element in destination collection
     * @param source      - the collection to predicate, must not be null
     * @param transformer - the transformer for the collection, must not be null
     * @return a transformed collection backed by the given collection
     * @throws java.lang.IllegalArgumentException
     *          - if the Collection or Transformer is null
     */
    public static <S, D> Collection<D> transform(Collection<S> source,
                                                 Transformer<S, D> transformer) {
        if (source == null) {
            throw new IllegalArgumentException("Source parameter is required");
        }
        else if (transformer == null) {
            throw new IllegalArgumentException(
                    "Transformer parameter is required");
        }

        List<D> dest = new ArrayList<D>();
        for (S sourceElem : source) {
            dest.add(transformer.transform(sourceElem));
        }
        return dest;
    }
    
    /**
     * Defines a functor interface implemented by classes that transform one
     * object into another.
     *
     * @param <S>
     * - type of source element
     * @param <D>
     * - type of destination element
     */
    public static interface Transformer<S, D> {
        /**
         * Transforms the input object (leaving it unchanged) into some output
         * object.
         *
         * @param t - the object to be transformed, should be left unchanged
         * @return a transformed object
         */
        D transform(S t);
    }

    /**
     * Defines a functor interface implemented by classes that perform a
     * predicate test on an object.
     *
     * @param <T>
     * - type of tested object
     */
    public static interface Predicate<T> {
        /**
         * Use the specified parameter to perform a test that returns true or
         * false.
         *
         * @param object - tested object
         * @return true or false
         */
        boolean evaluate(T object);
    }

    public static <S> String concatenate(S[] values, String delimiter,
                                         Transformer<S, String> transformer) {
        if (values == null || values.length == 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0, count = values.length; i < count; i++) {
            sb.append(transformer.transform(values[i]));
            if (i != count - 1) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    public static <S> String concatenate(S[] values, String delimiter) {
        if (values == null || values.length == 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0, count = values.length; i < count; i++) {
            sb.append(values[i].toString());
            if (i != count - 1) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    public static <T> void removeElements(Collection<T> collection,
                                          Predicate<T> predicate) {
        if (collection == null || predicate == null) {
            throw new IllegalArgumentException();
        }

        List<T> elementsToRemove = findElements(collection, predicate);
        collection.removeAll(elementsToRemove);
    }

    public static <T> List<T> findElements(final Collection<T> collection, final Predicate<T> predicate) {
        if (collection == null || predicate == null) {
            throw new IllegalArgumentException();
        }

        final List<T> elements = new ArrayList<T>();

        for (T elem : collection) {
            if (predicate.evaluate(elem)) {
                elements.add(elem);
            }
        }

        return elements;
    }

    /**
     * Defines a functor interface implemented by classes that do something.
     *
     * @author Khomushko
     * @param <T>
     */
    public static interface Closure<T> {
        /**
         * Performs an action on the specified input object.
         *
         * @param input
         */
        void execute(T input);
    }

    public static <T> void forAllDo(Collection<T> items,
                                    Closure<T> closure) {

        if (closure == null) {
            throw new IllegalArgumentException("The closure argument can't be null");
        }

        for (T item : items) {
            closure.execute(item);
        }
    }
    
    public static <T> List<T> filter(Collection<T> collection, Predicate<T> predicate) {
        List<T> retValue = new ArrayList<T>();
        if (collection == null || predicate == null) {
            throw new IllegalArgumentException();
        }

        for (T elem : collection) {
            if (predicate.evaluate(elem)) {
                retValue.add(elem);
                break;
            }
        }
        return retValue;
    }
}
