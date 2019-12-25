package edu.internet2.middleware.grouper.app.usdu;

import static edu.internet2.middleware.grouper.app.usdu.UsduAttributeNames.SUBJECT_RESOLUTION_DATE_LAST_RESOLVED;
import static edu.internet2.middleware.grouper.app.usdu.UsduSettings.usduStemName;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils;
import edu.internet2.middleware.subject.Source;

public class UsduService {
  
  
  /**
   * retrieve subject resolution attribute value for a given member if it exists or null
   * @param member
   * @return SubjectResolutionAttributeValue or null
   */
  public static SubjectResolutionAttributeValue getSubjectResolutionAttributeValue(Member member) {
    
    AttributeAssign attributeAssign = getAttributeAssign(member);
    if (attributeAssign == null) {
      return null;
    }
    
    return buildSubjectResolutionAttributeValue(attributeAssign, member);
  }
  
  /**
   * save or update subject resolution metadata attributes on a given member
   * @param subjectResolutionAttributeValue
   * @param member
   */
  public static void markMemberAsUnresolved(SubjectResolutionAttributeValue subjectResolutionAttributeValue, Member member) {
    
    AttributeAssign attributeAssign = getAttributeAssign(member);
    
    if (attributeAssign == null) {
      attributeAssign = member.getAttributeDelegate().assignAttribute(UsduAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
    }
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_RESOLVABLE, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), subjectResolutionAttributeValue.getSubjectResolutionResolvableString());
    
    if (StringUtils.isNotBlank(subjectResolutionAttributeValue.getSubjectResolutionDateLastResolvedString())) {
      attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DATE_LAST_RESOLVED, true);
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), subjectResolutionAttributeValue.getSubjectResolutionDateLastResolvedString());
    }
    
    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DAYS_UNRESOLVED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), subjectResolutionAttributeValue.getSubjectResolutionDaysUnresolvedString());
    
    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_LAST_CHECKED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), subjectResolutionAttributeValue.getSubjectResolutionDateLastCheckedString());
    
    // clear the rest
    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DELETED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), null);
        
    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DELETE_DATE, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), null);
    
    attributeAssign.saveOrUpdate();
    
  }
  
  /**
   * set subject resolution attributes on member
   * @param member
   */
  public static void markMemberAsDeleted(Member member) {
    
    AttributeAssign attributeAssign = getAttributeAssign(member);
    
    if (attributeAssign == null) {
      attributeAssign = member.getAttributeDelegate().assignAttribute(UsduAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
    }
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DELETED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), BooleanUtils.toStringTrueFalse(true));

    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DELETE_DATE, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), String.valueOf(new Date().getTime()));
    
    // clear the rest
    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DAYS_UNRESOLVED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), null);
        
    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_LAST_CHECKED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), null);
    
    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_RESOLVABLE, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), null);
    
    attributeAssign.saveOrUpdate();
    
  }
  
  
  /**
   * delete resolution attributes from the given member assignment
   * @param member
   */
  public static void deleteAttributeAssign(Member member) {
    AttributeAssign currentAttributeAssign = getAttributeAssign(member);
    if (currentAttributeAssign != null) {
      currentAttributeAssign.delete();
    }
  }
  
  /**
   * 
   * @return the list of subject sources with unresolved and resolved count
   */
  public static List<SubjectResolutionStat> getSubjectResolutionStats() {
    
    Set<Source> sources = SubjectFinder.getSources();
    
    GrouperSession session = GrouperSession.startRootSession();
    
    List<SubjectResolutionStat> subjectResolutionStats = new ArrayList<SubjectResolutionStat>();
    
    // select subject_source, count(*) from grouper_members group by subject_source
    List<Object[]> sourceIdCountTotals = HibernateSession.bySqlStatic().listSelect(Object[].class,
        "select subject_source, count(*) from grouper_members group by subject_source ", null, null);
    
    // select distinct source_id, subject_id from grouper_aval_asn_asn_member_v where attribute_def_name_name1 = 'penn:etc:usdu:subjectResolutionMarker'
    // and attribute_def_name_name2 = 'penn:etc:usdu:subjectResolutionResolvable' and value_string = 'false' and enabled2 = 'T';
    
    // select source_id, count(*) from grouper_aval_asn_asn_member_v where attribute_def_name_name1 = 'penn:etc:usdu:subjectResolutionMarker'
    // and attribute_def_name_name2 = 'penn:etc:usdu:subjectResolutionResolvable' and value_string = 'false' and enabled2 = 'T' group by source_id;
    
    final String sqlUnresolvable = "select source_id, count(*) from grouper_aval_asn_asn_member_v "
        + "where attribute_def_name_name1 = '" + UsduAttributeNames.retrieveAttributeDefNameBase().getName() + "' "
        + "and attribute_def_name_name2 = '" + UsduSettings.usduStemName() + ":" + UsduAttributeNames.SUBJECT_RESOLUTION_RESOLVABLE + "' "
        + "and value_string = 'false' and enabled2 = 'T'  group by source_id";
    List<Object[]> sourceIdCountUnresolvables = HibernateSession.bySqlStatic().listSelect(Object[].class, sqlUnresolvable, null, null);
    

    // select source_id, count(*) from grouper_aval_asn_asn_member_v where attribute_def_name_name1 = 'penn:etc:usdu:subjectResolutionMarker'
    // and attribute_def_name_name2 = 'penn:etc:usdu:subjectResolutionDeleted' and value_string = 'true' and enabled2 = 'T' group by source_id;
    
    final String sqlDeleted = "select source_id, count(*) from grouper_aval_asn_asn_member_v "
        + "where attribute_def_name_name1 = '" + UsduAttributeNames.retrieveAttributeDefNameBase().getName() + "' "
        + "and attribute_def_name_name2 = '" + UsduSettings.usduStemName() + ":" + UsduAttributeNames.SUBJECT_RESOLUTION_DELETED + "' "
        + "and value_string = 'true' and enabled2 = 'T'  group by source_id";
    List<Object[]> sourceIdCountDeleteds = HibernateSession.bySqlStatic().listSelect(Object[].class, sqlDeleted, null, null);
    

    for (Source source: sources) {
      
      long unresolvedCount = 0L;
      long resolvedCount = 0L;
      long deletedCount = 0L;

      // go through the amount received from totals
      TOTALS:
      for (Object[] sourceIdCountTotal : GrouperUtil.nonNull(sourceIdCountTotals)) {
        String sourceIdTotal = (String)sourceIdCountTotal[0];
        if (!StringUtils.equals(source.getId(), sourceIdTotal)) {
          continue TOTALS;
        }
        resolvedCount = GrouperUtil.intValue(sourceIdCountTotal[1]);

        UNRESOLVABLES:
        for (Object[] sourceIdCountUnresolvable : GrouperUtil.nonNull(sourceIdCountUnresolvables)) {
          String sourceIdUnresolvable = (String)sourceIdCountUnresolvable[0];
          if (!StringUtils.equals(source.getId(), sourceIdUnresolvable)) {
            continue UNRESOLVABLES;
          }
          
          // if there are unresolvables
          unresolvedCount = GrouperUtil.intValue(sourceIdCountUnresolvable[1]);
          resolvedCount -= unresolvedCount;
          
          DELETEDS:
          for (Object[] sourceIdCountDeleted : GrouperUtil.nonNull(sourceIdCountDeleteds)) {
            String sourceIdDeleted = (String)sourceIdCountDeleted[0];
            if (!StringUtils.equals(source.getId(), sourceIdDeleted)) {
              continue DELETEDS;
            }
            deletedCount = GrouperUtil.intValue(sourceIdCountUnresolvable[1]);
            unresolvedCount -= deletedCount;
            break DELETEDS;
          }
          
          break UNRESOLVABLES;
        }
        
        break TOTALS;
      }

      subjectResolutionStats.add(new SubjectResolutionStat(source.getName(), unresolvedCount, resolvedCount, deletedCount));
      
    }
    
    return subjectResolutionStats;
    
  }
  
  /**
   * 
   * @param queryOptions
   * @return unresolved subjects
   */
  public static Set<SubjectResolutionAttributeValue> getUnresolvedSubjects(QueryOptions queryOptions) {
    
    Set<SubjectResolutionAttributeValue> unresolvedSubjects = new HashSet<SubjectResolutionAttributeValue>();
    
    Set<Member> unresolvedMembers = new MemberFinder()
        .assignAttributeCheckReadOnAttributeDef(false)
        .assignNameOfAttributeDefName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_RESOLVABLE)
        .addAttributeValuesOnAssignment("false")
        .assignQueryOptions(queryOptions)
        .findMembers();
    
    Set<Member> deletedMembers = new MemberFinder()
        .assignAttributeCheckReadOnAttributeDef(false)
        .assignNameOfAttributeDefName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DELETED)
        .addAttributeValuesOnAssignment("true")
        .assignQueryOptions(queryOptions)
        .findMembers();
    
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    Date currentDate = new Date();
    Calendar c = Calendar.getInstance();
    
    for (Member member: unresolvedMembers) {
      
      String sourceId = member.getSubjectSourceId();
      
      Integer deleteAfterDays = GrouperConfig.retrieveConfig().propertyValueInt("usdu.source."+sourceId+".delete.ifAfterDays");
      if (deleteAfterDays == null) {
        deleteAfterDays = GrouperConfig.retrieveConfig().propertyValueInt("usdu.delete.ifAfterDays", 30);
      }
      
      SubjectResolutionAttributeValue subjectResolutionAttributeValue = getSubjectResolutionAttributeValue(member);
        
      Long daysSubjectHasBeenUnresolved = subjectResolutionAttributeValue.getSubjectResolutionDaysUnresolved();
      Long daysAfterWhichSubjectWillBeDeleted = deleteAfterDays - daysSubjectHasBeenUnresolved;
      
      c.setTime(currentDate);
      c.add(Calendar.DATE, daysAfterWhichSubjectWillBeDeleted.intValue());
      String dateSubjectWillBeDeleted = dateFormat.format(c.getTime());
      
      subjectResolutionAttributeValue.setDateSubjectWillBeDeletedString(dateSubjectWillBeDeleted);
      
      unresolvedSubjects.add(subjectResolutionAttributeValue);
    }
    
    for (Member member: deletedMembers) {
      
      SubjectResolutionAttributeValue subjectResolutionAttributeValue = getSubjectResolutionAttributeValue(member);
      
      String deletedDate = subjectResolutionAttributeValue.getSubjectResolutionDateDelete();
      subjectResolutionAttributeValue.setDateSubjectWillBeDeletedString(deletedDate);
      
      unresolvedSubjects.add(subjectResolutionAttributeValue);
      
    }
    
    return unresolvedSubjects;
    
  }
  
  
  /**
   * build subject resolution attribute object from member attributes
   * @param attributeAssign
   * @param member
   * @return SubjectResolutionAttributeValue
   */
  private static SubjectResolutionAttributeValue buildSubjectResolutionAttributeValue(AttributeAssign attributeAssign, Member member) {
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    
    SubjectResolutionAttributeValue result = new SubjectResolutionAttributeValue();
    result.setMember(member);
    
    AttributeAssignValue assignValue = attributeValueDelegate.retrieveAttributeAssignValue(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_RESOLVABLE);
    if (assignValue != null) {      
      result.setSubjectResolutionResolvableString(assignValue.getValueString());
    }
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(usduStemName()+":"+SUBJECT_RESOLUTION_DATE_LAST_RESOLVED);
    if (assignValue != null) {      
      result.setSubjectResolutionDateLastResolvedString(assignValue.getValueString());
    }
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DAYS_UNRESOLVED);
    if (assignValue != null) {      
      result.setSubjectResolutionDaysUnresolvedString(assignValue.getValueString());
    }
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_LAST_CHECKED);
    if (assignValue != null) {      
      result.setSubjectResolutionDateLastCheckedString(assignValue.getValueString());
    }
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DELETED);
    if (assignValue != null) {      
      result.setSubjectResolutionDeletedString(assignValue.getValueString());
    }
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DELETE_DATE);
    if (assignValue != null) {
      result.setSubjectResolutionDateDeleteString(assignValue.getValueString());
    }

    return result;
  }
  
  /**
   * get subject resolution attributes for a given member if it exists or null
   * @param member
   * @return
   */
  private static AttributeAssign getAttributeAssign(Member member) {
    
    Set<AttributeAssign> attributeAssigns = member.getAttributeDelegate().retrieveAssignments(UsduAttributeNames.retrieveAttributeDefNameBase());
    
    if (attributeAssigns.isEmpty()) {
      return null;
    }
    
    return attributeAssigns.iterator().next();
    
  }


}