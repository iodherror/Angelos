package cn.qcsoft.angelos.util

import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector
import com.fasterxml.jackson.databind.introspect.AnnotatedMember

/**
 * Created by QC on 2018/9/11 17:01.
 */
class JooqJacksonIgoreInherited : JacksonAnnotationIntrospector(){

    override fun hasIgnoreMarker(m: AnnotatedMember): Boolean {
        return m.declaringClass.name.contains("org.jooq") || super.hasIgnoreMarker(m)
    }
}