package reactor.core.publisher;

import reactor.util.context.Context;

interface ContextHolder {
   Context currentContext();
}
