import { useDslEditorPage } from '../../../hooks/useDslEditorPage'
import DslEditorWorkspace from '../components/DslEditorWorkspace'

export default function DslEditorContainer() {
  const dslEditor = useDslEditorPage()

  return <DslEditorWorkspace {...dslEditor} />
}
