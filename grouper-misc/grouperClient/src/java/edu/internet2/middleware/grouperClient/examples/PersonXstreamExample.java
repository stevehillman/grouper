/*
 * @author mchyzer
 * $Id: PersonXstreamExample.java,v 1.1 2008-11-27 14:25:53 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.examples;

import edu.internet2.middleware.grouperClient.ext.com.thoughtworks.xstream.XStream;
import edu.internet2.middleware.grouperClient.ext.com.thoughtworks.xstream.annotations.XStreamOmitField;
import edu.internet2.middleware.grouperClient.ext.com.thoughtworks.xstream.converters.ConversionException;
import edu.internet2.middleware.grouperClient.ext.com.thoughtworks.xstream.io.xml.DomDriver;
import edu.internet2.middleware.grouperClient.ext.com.thoughtworks.xstream.mapper.MapperWrapper;
import edu.internet2.middleware.grouperClient.ext.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 *
 */
public class PersonXstreamExample {

  /**
   * 
   */
  static Log log = GrouperClientUtils.retrieveLog(PersonXstreamExample.class);

  /**
   * start of request
   */
  @SuppressWarnings("unused")
  @XStreamOmitField
  private long millisStart = 500;

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    PersonXstreamExample person = new PersonXstreamExample();
    person.setName("hey");
    XStream xStream = new XStream(new DomDriver()) {

      /**
       * 
       * @see edu.internet2.middleware.grouperClient.ext.com.thoughtworks.xstream.XStream#wrapMapper(edu.internet2.middleware.grouperClient.ext.com.thoughtworks.xstream.mapper.MapperWrapper)
       */
      @Override
      protected MapperWrapper wrapMapper(MapperWrapper next) {
        return new MapperWrapper(next) {

          /**
           * 
           * @see edu.internet2.middleware.grouperClient.ext.com.thoughtworks.xstream.mapper.MapperWrapper#shouldSerializeMember(java.lang.Class, java.lang.String)
           */
          @Override
          public boolean shouldSerializeMember(Class definedIn, String fieldName) {
            boolean definedInNotObject = definedIn != Object.class;
            if (definedInNotObject) {
              return super.shouldSerializeMember(definedIn, fieldName);
            }

            log.info("Cant find field: " + fieldName);
            return false;
          }

        };
      }

    };

    xStream.autodetectAnnotations(true);
    xStream.alias("person", PersonXstreamExample.class);
    String xml = xStream.toXML(person);
    System.out.println(xml);

    PersonXstreamExample newPerson = (PersonXstreamExample) xStream.fromXML(xml);
    System.out.println(newPerson);
    try {
      newPerson = (PersonXstreamExample) xStream
          .fromXML("<person><name>hey</name><whatever>whatever</whatever></person>");
      System.out.println(newPerson);
    } catch (ConversionException ce) {
      ce.printStackTrace();
      throw new RuntimeException(ce);
    }

  }

  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Person: " + this.name;
  }

  /**
   * 
   */
  private String name;

  /**
   * 
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * 
   * @param name1
   */
  public void setName(String name1) {
    this.name = name1;
  }

}
