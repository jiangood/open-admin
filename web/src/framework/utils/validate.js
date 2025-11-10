
export const ValidateUtil = {
    isEmail(emailStr) {
        const reg = /^([\w+\.])+@\w+([.]\w+)+$/;
        return reg.test(emailStr)
    }
}


