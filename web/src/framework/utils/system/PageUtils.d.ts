export class PageUtils {
    static redirectToLogin(): void;
    static currentParams(): Record<string, string | undefined>;
    static currentPathname(): string;
    static currentUrl(): string;
    static currentPathnameLastPart(): string | undefined;
    static open(path: string, label?: string): void;
    static openNoLayout(path: string): void;
    static currentLabel(): string | undefined;
    static closeCurrent(): void;
    static closeCurrentAndOpenPage(alertMessage: string, path: string, label: string): void;
}
