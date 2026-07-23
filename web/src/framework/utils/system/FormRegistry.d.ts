import React from 'react';

export class FormRegistry {
    static register(formKey: string, formComponent: React.ComponentType<any>): void;
    static get(formKey: string): React.ComponentType<any> | null;
    static has(formKey: string): boolean;
    static getAllKeys(): string[];
}
