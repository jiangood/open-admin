// logger.js
export class Logger {

    private readonly module: string;


    constructor(module) {
        this.module = module;
    }

    public static getLogger(module) {
        return new Logger(module);
    }

    debug(...args) {
        this._log( console.log, args);
    }

    info(...args) {
        this._log( console.info, args);
    }

    warn(...args) {
        this._log(console.warn, args);
    }

    error(...args) {
        this._log(console.error, args);
    }

    private _log( consoleMethod, args) {
        const time = new Date().toLocaleTimeString();
        consoleMethod(`[${time}] [${this.module}] `, ...args);
    }


}



