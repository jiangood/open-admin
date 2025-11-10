import React from 'react';

export interface SwitchYNProps {
  mode?: string | 'read';
  value?: String;
  onChange?: (value: String) => void;
}

declare class SwitchYN extends React.Component<SwitchYNProps, any> {}

declare class FieldSwitchYN extends React.Component<SwitchYNProps, any> {}
