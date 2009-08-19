/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.internal;

import org.gradle.api.plugins.Convention;
import org.gradle.api.tasks.ConventionValue;
import org.gradle.util.GUtil;
import static org.gradle.util.WrapUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class DefaultClassGeneratorTest {
    private final DefaultClassGenerator generator = new DefaultClassGenerator();

    @Test
    public void mixesInConventionAwareInterface() throws Exception {
        Class<? extends Bean> generatedClass = generator.generate(Bean.class);
        assertTrue(IConventionAware.class.isAssignableFrom(generatedClass));
        Bean bean = generatedClass.newInstance();
        ((IConventionAware) bean).conventionMapping(toMap("property", (ConventionValue) null));
    }

    @Test
    public void testCachesGeneratedSubclass() {
        assertEquals(generator.generate(Bean.class), generator.generate(Bean.class));
    }
    
    @Test
    public void overridesPublicConstructors() throws Exception {
        Class<? extends Bean> generatedClass = generator.generate(BeanWithConstructor.class);
        Bean bean = generatedClass.getConstructor(String.class).newInstance("value");
        assertThat(bean.getProperty(), equalTo("value"));

        bean = generatedClass.getConstructor().newInstance();
        assertThat(bean.getProperty(), equalTo("default value"));
    }

    @Test
    public void appliesConventionMappingToEachGetter() throws Exception {
        Class<? extends Bean> generatedClass = generator.generate(Bean.class);
        assertTrue(IConventionAware.class.isAssignableFrom(generatedClass));
        Bean bean = generatedClass.newInstance();
        IConventionAware conventionAware = (IConventionAware) bean;

        assertThat(bean.getProperty(), nullValue());

        conventionAware.conventionMapping(GUtil.map("property", new ConventionValue() {
            public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                return "conventionValue";
            }
        }));

        assertThat(bean.getProperty(), equalTo("conventionValue"));

        bean.setProperty("value");
        assertThat(bean.getProperty(), equalTo("value"));
    }

    @Test
    public void handlesVariousPropertyTypes() {
        generator.generate(BeanWithVariousPropertyTypes.class);
    }

    @Test
    public void doesNotOverrideMethodsFromConventionAwareInterface() throws Exception {
        Class<? extends ConventionAwareBean> generatedClass = generator.generate(ConventionAwareBean.class);
        assertTrue(IConventionAware.class.isAssignableFrom(generatedClass));
        ConventionAwareBean bean = generatedClass.newInstance();
        bean.conventionMapping(GUtil.map(
                "property", new ConventionValue(){
                    public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                        throw new UnsupportedOperationException();
                    }
                }));
        
        assertSame(bean.mapping, bean.getConventionMapping());

        // Check conv() is not overridden
        bean.setProperty("value");
        assertEquals("[value]", bean.getProperty());
    }

    @Test
    public void doesNotOverrideMethodsFromSuperclassesMarkedWithAnnotation() throws Exception {
        BeanSubClass bean = generator.generate(BeanSubClass.class).newInstance();
        IConventionAware conventionAware = (IConventionAware) bean;
        conventionAware.conventionMapping(GUtil.map(
                "property", new ConventionValue(){
                    public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                        throw new UnsupportedOperationException();
                    }
                },
                "interfaceProperty", new ConventionValue(){
                    public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                        throw new UnsupportedOperationException();
                    }
                },
                "overriddenProperty", new ConventionValue(){
                    public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                        return "conventionValue";
                    }
                },
                "otherProperty", new ConventionValue(){
                    public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                        return "conventionValue";
                    }
                }));
        assertEquals(null, bean.getProperty());
        assertEquals(null, bean.getInterfaceProperty());
        assertEquals("conventionValue", bean.getOverriddenProperty());
        assertEquals("conventionValue", bean.getOtherProperty());
    }

    @Test
    public void doesNotGenerateSubclassForClassMarkedWithAnnotation() {
        assertEquals(NoMappingBean.class, generator.generate(NoMappingBean.class));
    }

    public static class Bean {
        private String property;

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }
    }

    public static class BeanWithConstructor extends Bean {
        public BeanWithConstructor() {
            this("default value");
        }

        public BeanWithConstructor(String value) {
            setProperty(value);
        }
    }

    public static class ConventionAwareBean extends Bean implements IConventionAware {
        Map<String, ConventionValue> mapping = new HashMap<String, ConventionValue>();

        public Object conv(Object internalValue, String propertyName) {
            if (internalValue instanceof String) {
                return "[" + internalValue + "]";
            } else {
                throw new UnsupportedOperationException();
            }
        }

        public Object conventionMapping(Map<String, ConventionValue> mapping) {
            return this;
        }

        public Map<String, ConventionValue> getConventionMapping() {
            return mapping;
        }

        public void setConventionMapping(Map<String, ConventionValue> conventionMapping) {
            this.mapping = conventionMapping;
        }
    }

    public static class BeanWithVariousPropertyTypes {
        public String[] getArrayProperty() {
            return null;
        }

        public boolean getBoooleanProperty() {
            return false;
        }
    }

    public interface SomeType {
        String getInterfaceProperty();
    }

    @NoConventionMapping
    public static class NoMappingBean implements SomeType {
        public String getProperty() {
            return null;
        }

        public String getInterfaceProperty() {
            return null;
        }

        public String getOverriddenProperty() {
            return null;
        }
    }

    public static class BeanSubClass extends NoMappingBean {
        @Override
        public String getOverriddenProperty() {
            return null;
        }

        public String getOtherProperty() {
            return null;
        }
    }
}