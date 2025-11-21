package io.admin.modules.api.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.admin.common.utils.ann.Remark;
import io.admin.common.utils.field.FieldDescription;
import io.admin.modules.api.ApiMapping;
import io.admin.modules.api.entity.ApiResource;
import io.admin.modules.api.entity.ApiResourceArgument;
import io.admin.modules.api.entity.ApiResourceArgumentReturn;
import jakarta.annotation.Resource;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@Service
public class ApiResourceService {

    private Map<String, ApiResource> actionMap = new LinkedHashMap<>();






    public List<ApiResource> findAll() {
        return actionMap.values().stream().toList();
    }


    @Transactional
    public void add(ApiResource r) {
        actionMap.put(r.getAction(), r);
    }




    @Transactional
    public void saveOrUpdate(String beanName, Object bean) {
        Method[] methods = bean.getClass().getMethods();

        for (Method method : methods) {
            ApiMapping api = method.getAnnotation(ApiMapping.class);
            if (api == null) {
                return;
            }


            String action = api.action();
            ApiResource r = this.findAction(action);
            if (r == null) {
                r = new ApiResource();
            }

            Assert.state(!action.contains("/"), "action不能包含斜杠: " + action);
            r.setName(api.name());
            r.setAction(action);
            r.setDesc(api.desc());
            r.setBeanName(beanName);

            r.setBean(bean);
            r.setMethod(method);

            r.setReturnType(method.getReturnType().getSimpleName());
            r.setParameterList(parseArgs(method));
            r.setReturnList(parseReturnArgs(method));

            this.add(r);
        }
    }

    private List<ApiResourceArgument> parseArgs(Method method) {

        Class<?>[] parameters = method.getParameterTypes();
        StandardReflectionParameterNameDiscoverer u = new StandardReflectionParameterNameDiscoverer();
        String[] paramNames = u.getParameterNames(method);

        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        List<ApiResourceArgument> parameterList = new ArrayList<>();
        for (int i = 0; i < parameters.length; i++) {
            String type = parameters[i].getSimpleName();
            String name = paramNames[i];

            ApiResourceArgument a = new ApiResourceArgument();
            a.setName(name);
            a.setType(type);
            a.setIndex(i);


            Annotation[] anns = parameterAnnotations[i];
            if (anns.length > 0) {
                Annotation ann = anns[0];
                if (ann instanceof FieldDescription f) {
                    a.setRequired(f.required());
                    a.setDesc(f.label());
                    a.setDemo(f.demo());
                    if (f.len() > 0) {
                        a.setLen(f.len());
                    }
                }
            }

            parameterList.add(a);
        }
        return parameterList;
    }


    public List<ApiResourceArgumentReturn> parseReturnArgs(Method method) {
        Class<?> returnType = method.getReturnType();


        boolean isSimpleType = returnType.isPrimitive() || String.class.isAssignableFrom(returnType);
        if (isSimpleType) {
            return null;
        }

        boolean isCollection = Collection.class.isAssignableFrom(returnType);
        if (isCollection) {
            ParameterizedType parameterizedType = (ParameterizedType) method.getGenericReturnType();// 泛型
            Type type = parameterizedType.getActualTypeArguments()[0];

            return getClassFields((Class<?>) type);
        }
        return getClassFields(returnType);
    }


    private List<ApiResourceArgumentReturn> getClassFields(Class<?> cls) {
        List<ApiResourceArgumentReturn> list = new ArrayList<>();
        for (Field field : cls.getDeclaredFields()) {
            JsonIgnore jsonIgnore = field.getAnnotation(JsonIgnore.class);
            if (jsonIgnore != null) {
                continue;
            }

            ApiResourceArgumentReturn dict = new ApiResourceArgumentReturn();
            dict.setName(field.getName());
            dict.setType(field.getType().getSimpleName());

            FieldDescription f = field.getAnnotation(FieldDescription.class);
            if (f != null) {
                dict.setRequired(f.required());
                dict.setDesc(f.label());
                dict.setDemo(f.demo());
            } else {
                Remark msg = field.getAnnotation(Remark.class);
                if (msg != null) {
                    dict.setDesc(msg.value());
                }
            }

            list.add(dict);
        }
        return list;
    }

    public ApiResource findAction(String action) {
        return actionMap.get(action);
    }
}
