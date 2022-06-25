package io.micronaut.web.router;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;
import groovy.transform.Generated;
import groovy.transform.Internal;
import io.micronaut.context.ExecutionHandleLocator;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.naming.conventions.PropertyConvention;
import io.micronaut.http.HttpStatus;
import java.beans.Transient;
import org.codehaus.groovy.runtime.MethodClosure;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.runtime.callsite.CallSite;

public class GroovyRouteBuilder extends DefaultRouteBuilder implements GroovyObject {
   @Generated
   public GroovyRouteBuilder(ExecutionHandleLocator executionHandleLocator) {
      CallSite[] var2 = $getCallSiteArray();
      super(executionHandleLocator);
      MetaClass var3 = this.$getStaticMetaClass();
      this.metaClass = var3;
   }

   @Generated
   public GroovyRouteBuilder(ExecutionHandleLocator executionHandleLocator, RouteBuilder.UriNamingStrategy uriNamingStrategy) {
      CallSite[] var3 = $getCallSiteArray();
      super(executionHandleLocator, uriNamingStrategy);
      MetaClass var4 = this.$getStaticMetaClass();
      this.metaClass = var4;
   }

   @Generated
   public GroovyRouteBuilder(
      ExecutionHandleLocator executionHandleLocator, RouteBuilder.UriNamingStrategy uriNamingStrategy, ConversionService<?> conversionService
   ) {
      CallSite[] var4 = $getCallSiteArray();
      super(executionHandleLocator, uriNamingStrategy, conversionService);
      MetaClass var5 = this.$getStaticMetaClass();
      this.metaClass = var5;
   }

   public StatusRoute status(HttpStatus httpStatus, MethodClosure methodClosure) {
      CallSite[] var3 = $getCallSiteArray();
      return (StatusRoute)ScriptBytecodeAdapter.castToType(
         var3[0]
            .callCurrent(
               this,
               httpStatus,
               var3[1].call(var3[2].callGroovyObjectGetProperty(methodClosure)),
               var3[3].callGroovyObjectGetProperty(methodClosure),
               var3[4].callGroovyObjectGetProperty(methodClosure)
            ),
         StatusRoute.class
      );
   }

   public ResourceRoute resources(Object target, @DelegatesTo(GroovyRouteBuilder.class) Closure nested) {
      CallSite[] var3 = $getCallSiteArray();
      return (ResourceRoute)ScriptBytecodeAdapter.castToType(var3[5].call(var3[6].callCurrent(this, target), nested), ResourceRoute.class);
   }

   public Route GET(Object target, PropertyConvention id, @DelegatesTo(GroovyRouteBuilder.class) Closure nested) {
      CallSite[] var4 = $getCallSiteArray();
      return (Route)ScriptBytecodeAdapter.castToType(var4[7].call(var4[8].callCurrent(this, target, id), nested), Route.class);
   }

   public Route GET(Object target, @DelegatesTo(GroovyRouteBuilder.class) Closure nested) {
      CallSite[] var3 = $getCallSiteArray();
      return (Route)ScriptBytecodeAdapter.castToType(var3[9].call(var3[10].callCurrent(this, target), nested), Route.class);
   }

   public Route GET(String uri, MethodClosure methodClosure) {
      CallSite[] var3 = $getCallSiteArray();
      return (Route)ScriptBytecodeAdapter.castToType(
         var3[11]
            .callCurrent(
               this,
               uri,
               var3[12].callGroovyObjectGetProperty(methodClosure),
               var3[13].callGroovyObjectGetProperty(methodClosure),
               var3[14].callGroovyObjectGetProperty(methodClosure)
            ),
         Route.class
      );
   }

   public Route POST(Object target, PropertyConvention id, @DelegatesTo(GroovyRouteBuilder.class) Closure nested) {
      CallSite[] var4 = $getCallSiteArray();
      return (Route)ScriptBytecodeAdapter.castToType(var4[15].call(var4[16].callCurrent(this, target, id), nested), Route.class);
   }

   public Route POST(Object target, @DelegatesTo(GroovyRouteBuilder.class) Closure nested) {
      CallSite[] var3 = $getCallSiteArray();
      return (Route)ScriptBytecodeAdapter.castToType(var3[17].call(var3[18].callCurrent(this, target), nested), Route.class);
   }

   public Route POST(String uri, MethodClosure methodClosure) {
      CallSite[] var3 = $getCallSiteArray();
      return (Route)ScriptBytecodeAdapter.castToType(
         var3[19]
            .callCurrent(
               this,
               uri,
               var3[20].callGroovyObjectGetProperty(methodClosure),
               var3[21].callGroovyObjectGetProperty(methodClosure),
               var3[22].callGroovyObjectGetProperty(methodClosure)
            ),
         Route.class
      );
   }

   public Route PUT(Object target, PropertyConvention id, @DelegatesTo(GroovyRouteBuilder.class) Closure nested) {
      CallSite[] var4 = $getCallSiteArray();
      return (Route)ScriptBytecodeAdapter.castToType(var4[23].call(var4[24].callCurrent(this, target, id), nested), Route.class);
   }

   public Route PUT(Object target, @DelegatesTo(GroovyRouteBuilder.class) Closure nested) {
      CallSite[] var3 = $getCallSiteArray();
      return (Route)ScriptBytecodeAdapter.castToType(var3[25].call(var3[26].callCurrent(this, target), nested), Route.class);
   }

   public Route PUT(String uri, MethodClosure methodClosure) {
      CallSite[] var3 = $getCallSiteArray();
      return (Route)ScriptBytecodeAdapter.castToType(
         var3[27]
            .callCurrent(
               this,
               uri,
               var3[28].callGroovyObjectGetProperty(methodClosure),
               var3[29].callGroovyObjectGetProperty(methodClosure),
               var3[30].callGroovyObjectGetProperty(methodClosure)
            ),
         Route.class
      );
   }

   public Route PATCH(Object target, PropertyConvention id, @DelegatesTo(GroovyRouteBuilder.class) Closure nested) {
      CallSite[] var4 = $getCallSiteArray();
      return (Route)ScriptBytecodeAdapter.castToType(var4[31].call(var4[32].callCurrent(this, target, id), nested), Route.class);
   }

   public Route PATCH(Object target, @DelegatesTo(GroovyRouteBuilder.class) Closure nested) {
      CallSite[] var3 = $getCallSiteArray();
      return (Route)ScriptBytecodeAdapter.castToType(var3[33].call(var3[34].callCurrent(this, target), nested), Route.class);
   }

   public Route PATCH(String uri, MethodClosure methodClosure) {
      CallSite[] var3 = $getCallSiteArray();
      return (Route)ScriptBytecodeAdapter.castToType(
         var3[35]
            .callCurrent(
               this,
               uri,
               var3[36].callGroovyObjectGetProperty(methodClosure),
               var3[37].callGroovyObjectGetProperty(methodClosure),
               var3[38].callGroovyObjectGetProperty(methodClosure)
            ),
         Route.class
      );
   }

   public Route DELETE(Object target, PropertyConvention id, @DelegatesTo(GroovyRouteBuilder.class) Closure nested) {
      CallSite[] var4 = $getCallSiteArray();
      return (Route)ScriptBytecodeAdapter.castToType(var4[39].call(var4[40].callCurrent(this, target, id), nested), Route.class);
   }

   public Route DELETE(Object target, @DelegatesTo(GroovyRouteBuilder.class) Closure nested) {
      CallSite[] var3 = $getCallSiteArray();
      return (Route)ScriptBytecodeAdapter.castToType(var3[41].call(var3[42].callCurrent(this, target), nested), Route.class);
   }

   public Route DELETE(String uri, MethodClosure methodClosure) {
      CallSite[] var3 = $getCallSiteArray();
      return (Route)ScriptBytecodeAdapter.castToType(
         var3[43]
            .callCurrent(
               this,
               uri,
               var3[44].callGroovyObjectGetProperty(methodClosure),
               var3[45].callGroovyObjectGetProperty(methodClosure),
               var3[46].callGroovyObjectGetProperty(methodClosure)
            ),
         Route.class
      );
   }

   public Route OPTIONS(Object target, PropertyConvention id, @DelegatesTo(GroovyRouteBuilder.class) Closure nested) {
      CallSite[] var4 = $getCallSiteArray();
      return (Route)ScriptBytecodeAdapter.castToType(var4[47].call(var4[48].callCurrent(this, target, id), nested), Route.class);
   }

   public Route OPTIONS(Object target, @DelegatesTo(GroovyRouteBuilder.class) Closure nested) {
      CallSite[] var3 = $getCallSiteArray();
      return (Route)ScriptBytecodeAdapter.castToType(var3[49].call(var3[50].callCurrent(this, target), nested), Route.class);
   }

   public Route OPTIONS(String uri, MethodClosure methodClosure) {
      CallSite[] var3 = $getCallSiteArray();
      return (Route)ScriptBytecodeAdapter.castToType(
         var3[51]
            .callCurrent(
               this,
               uri,
               var3[52].callGroovyObjectGetProperty(methodClosure),
               var3[53].callGroovyObjectGetProperty(methodClosure),
               var3[54].callGroovyObjectGetProperty(methodClosure)
            ),
         Route.class
      );
   }

   public Route HEAD(Object target, PropertyConvention id, @DelegatesTo(GroovyRouteBuilder.class) Closure nested) {
      CallSite[] var4 = $getCallSiteArray();
      return (Route)ScriptBytecodeAdapter.castToType(var4[55].call(var4[56].callCurrent(this, target, id), nested), Route.class);
   }

   public Route HEAD(Object target, @DelegatesTo(GroovyRouteBuilder.class) Closure nested) {
      CallSite[] var3 = $getCallSiteArray();
      return (Route)ScriptBytecodeAdapter.castToType(var3[57].call(var3[58].callCurrent(this, target), nested), Route.class);
   }

   public Route HEAD(String uri, MethodClosure methodClosure) {
      CallSite[] var3 = $getCallSiteArray();
      return (Route)ScriptBytecodeAdapter.castToType(
         var3[59]
            .callCurrent(
               this,
               uri,
               var3[60].callGroovyObjectGetProperty(methodClosure),
               var3[61].callGroovyObjectGetProperty(methodClosure),
               var3[62].callGroovyObjectGetProperty(methodClosure)
            ),
         Route.class
      );
   }

   @Generated
   @Internal
   @Transient
   public MetaClass getMetaClass() {
      MetaClass var10000 = this.metaClass;
      if (this.metaClass != null) {
         return var10000;
      } else {
         this.metaClass = this.$getStaticMetaClass();
         return this.metaClass;
      }
   }

   @Generated
   @Internal
   public void setMetaClass(MetaClass var1) {
      this.metaClass = var1;
   }
}
