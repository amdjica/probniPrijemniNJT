import { infoOStatusu } from '../utils/statusInfo.js';

export default function OznakaStatusa({ status }) {
  const { oznaka, ton } = infoOStatusu(status);
  return <span className={`oznaka oznaka--${ton}`}>{oznaka}</span>;
}
