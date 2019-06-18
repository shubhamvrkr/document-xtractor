import {Barcode} from "./Barcode"
import {Page} from "./Page"

export interface Template {
  pageCount: number;
  docCode: string;
  barcode: Barcode;
  pages:Map<string,Page>
}
